import { useState } from 'react';
import { openDB } from 'idb';
import VoiceInput from './components/VoiceInput';

const API_URL = 'http://localhost:8080/api/chat/ask';
const CACHE_DB_NAME = 'samadhan-offline-db';

// Function to save response to IndexedDB
const saveToOfflineCache = async (query, response, domain, language) => {
  try {
    const db = await openDB(CACHE_DB_NAME, 1, {
      upgrade(db) {
        if (!db.objectStoreNames.contains('responses')) {
          db.createObjectStore('responses', { keyPath: 'id', autoIncrement: true });
        }
      },
    });
    const tx = db.transaction('responses', 'readwrite');
    const store = tx.objectStore('responses');
    await store.add({ query, response, domain, language, timestamp: Date.now() });
    await tx.done;
    console.log('Saved to offline cache');
  } catch (error) {
    console.log('Offline cache save failed:', error);
  }
};

// Function to get cached response
const getFromOfflineCache = async (query, domain, language) => {
  try {
    const db = await openDB(CACHE_DB_NAME, 1);
    const tx = db.transaction('responses', 'readonly');
    const store = tx.objectStore('responses');
    const allItems = await store.getAll();
    
    // Find exact match
    const match = allItems.find(item => 
      item.query === query && item.domain === domain && item.language === language
    );
    return match ? match.response : null;
  } catch (error) {
    console.log('Offline cache read failed:', error);
    return null;
  }
};

function App() {
  const [query, setQuery] = useState('');
  const [response, setResponse] = useState('');
  const [loading, setLoading] = useState(false);
  const [domain, setDomain] = useState('agriculture');
  const [language, setLanguage] = useState('hi');
  const [isListening, setIsListening] = useState(false);
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  // Monitor online/offline status
  useState(() => {
    window.addEventListener('online', () => setIsOnline(true));
    window.addEventListener('offline', () => setIsOnline(false));
    return () => {
      window.removeEventListener('online', () => setIsOnline(true));
      window.removeEventListener('offline', () => setIsOnline(false));
    };
  }, []);

  const handleVoiceResult = async (text) => {
    setQuery(text);
    setLoading(true);
    
    try {
      // First, check offline cache
      const cachedResponse = await getFromOfflineCache(text, domain, language);
      
      if (cachedResponse) {
        console.log('Using cached response (offline mode)');
        setResponse(cachedResponse);
        // Speak the cached response
        const utterance = new SpeechSynthesisUtterance(cachedResponse);
        if (language === 'hi') utterance.lang = 'hi-IN';
        else if (language === 'mr') utterance.lang = 'mr-IN';
        else utterance.lang = 'en-US';
        window.speechSynthesis.speak(utterance);
        setLoading(false);
        return;
      }
      
      // If not in cache, check if online
      if (!isOnline) {
        setResponse('No internet connection and no cached response found. Please connect to internet for new queries.');
        setLoading(false);
        return;
      }
      
      // Online mode: call backend API
      const res = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: text, domain, language })
      });
      
      const data = await res.json();
      setResponse(data.response);
      
      // Save to offline cache for future use
      await saveToOfflineCache(text, data.response, domain, language);
      
      // Text to speech
      const utterance = new SpeechSynthesisUtterance(data.response);
      if (language === 'hi') utterance.lang = 'hi-IN';
      else if (language === 'mr') utterance.lang = 'mr-IN';
      else utterance.lang = 'en-US';
      
      window.speechSynthesis.speak(utterance);
      
    } catch (error) {
      console.error('API error:', error);
      // Try cache again on error
      const cachedResponse = await getFromOfflineCache(text, domain, language);
      if (cachedResponse) {
        setResponse(cachedResponse + ' (from offline cache)');
      } else {
        setResponse('Cannot connect to server. Please check if backend is running, or use cached queries.');
      }
    }
    setLoading(false);
  };

  const handleTextSubmit = async () => {
    if (!query.trim()) return;
    setLoading(true);
    
    try {
      // Check cache first
      const cachedResponse = await getFromOfflineCache(query, domain, language);
      if (cachedResponse) {
        setResponse(cachedResponse);
        const utterance = new SpeechSynthesisUtterance(cachedResponse);
        if (language === 'hi') utterance.lang = 'hi-IN';
        else if (language === 'mr') utterance.lang = 'mr-IN';
        window.speechSynthesis.speak(utterance);
        setLoading(false);
        return;
      }
      
      if (!isOnline) {
        setResponse('No internet connection. Please connect to internet for new queries.');
        setLoading(false);
        return;
      }
      
      const res = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query, domain, language })
      });
      
      const data = await res.json();
      setResponse(data.response);
      await saveToOfflineCache(query, data.response, domain, language);
      
      const utterance = new SpeechSynthesisUtterance(data.response);
      if (language === 'hi') utterance.lang = 'hi-IN';
      else if (language === 'mr') utterance.lang = 'mr-IN';
      window.speechSynthesis.speak(utterance);
      
    } catch (error) {
      const cachedResponse = await getFromOfflineCache(query, domain, language);
      if (cachedResponse) {
        setResponse(cachedResponse + ' (from offline cache)');
      } else {
        setResponse('Server error. Please try again.');
      }
    }
    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 via-blue-50 to-green-50 p-4">
      <div className="max-w-md mx-auto">
        {/* Header */}
        <div className="text-center mb-6">
          <h1 className="text-4xl font-bold text-green-800 mt-6 mb-2">
            Samadhan with AI
          </h1>
          <p className="text-gray-600">Voice-first assistant for rural India</p>
          {/* Online/Offline Indicator */}
          <div className={`inline-block mt-2 px-3 py-1 rounded-full text-xs font-medium ${isOnline ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {isOnline ? '🟢 Online' : '🔴 Offline Mode'}
          </div>
        </div>

        {/* Domain Selector */}
        <div className="flex gap-2 mb-4 justify-center">
          {[
            { id: 'agriculture', label: '🌾 Farming', color: 'bg-green-100' },
            { id: 'health', label: '🏥 Health', color: 'bg-blue-100' },
            { id: 'scheme', label: '📋 Schemes', color: 'bg-yellow-100' }
          ].map(d => (
            <button
              key={d.id}
              onClick={() => setDomain(d.id)}
              className={`px-4 py-2 rounded-full font-medium transition-all ${
                domain === d.id 
                  ? 'bg-green-600 text-white shadow-lg scale-105' 
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {d.label}
            </button>
          ))}
        </div>

        {/* Language Selector */}
        <div className="flex gap-2 mb-6 justify-center">
          {[
            { code: 'hi', name: 'हिंदी' },
            { code: 'mr', name: 'मराठी' },
            { code: 'en', name: 'English' }
          ].map(l => (
            <button
              key={l.code}
              onClick={() => setLanguage(l.code)}
              className={`px-4 py-1.5 rounded-lg transition-all ${
                language === l.code 
                  ? 'bg-blue-600 text-white shadow-md' 
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {l.name}
            </button>
          ))}
        </div>

        {/* Voice Button */}
        <div className="flex justify-center mb-8">
          <VoiceInput 
            onResult={handleVoiceResult} 
            language={language === 'hi' ? 'hi-IN' : language === 'mr' ? 'mr-IN' : 'en-US'}
            isListening={isListening}
            setIsListening={setIsListening}
          />
        </div>

        {/* Text Input Fallback */}
        <div className="mb-4">
          <textarea
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Or type your question here..."
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
            rows="2"
          />
          <button
            onClick={handleTextSubmit}
            disabled={loading}
            className="w-full mt-2 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition-colors"
          >
            {loading ? 'Thinking...' : 'Ask'}
          </button>
        </div>

        {/* Query Display */}
        {query && !isListening && (
          <div className="bg-white rounded-lg p-4 mb-4 shadow-md border-l-4 border-green-500">
            <p className="text-gray-500 text-sm font-medium">You asked:</p>
            <p className="text-lg font-medium text-gray-800">"{query}"</p>
          </div>
        )}

        {/* Loading */}
        {loading && (
          <div className="text-center py-8">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-green-600"></div>
            <p className="text-gray-500 mt-2">Getting answer...</p>
          </div>
        )}

        {/* Response Display */}
        {response && !loading && (
          <div className="bg-green-100 rounded-lg p-4 shadow-md border-l-4 border-green-600">
            <p className="text-gray-600 text-sm font-medium">Samadhan AI says:</p>
            <p className="text-lg text-gray-800 mt-1">{response}</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;