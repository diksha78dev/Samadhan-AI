import { useState, useEffect, useRef } from 'react';

export default function VoiceInput({ onResult, language = 'hi-IN', isListening, setIsListening }) {
  const recognitionRef = useRef(null);

  useEffect(() => {
    if (!('webkitSpeechRecognition' in window)) {
      alert('Voice input not supported. Please use Chrome browser.');
      return;
    }

    const SpeechRecognition = window.webkitSpeechRecognition;
    recognitionRef.current = new SpeechRecognition();
    recognitionRef.current.continuous = false;
    recognitionRef.current.interimResults = false;
    recognitionRef.current.lang = language;

    recognitionRef.current.onresult = (event) => {
      const text = event.results[0][0].transcript;
      onResult(text);
      setIsListening(false);
    };

    recognitionRef.current.onerror = () => setIsListening(false);
    recognitionRef.current.onend = () => setIsListening(false);

    return () => {
      if (recognitionRef.current) {
        recognitionRef.current.abort();
      }
    };
  }, [language]);

  useEffect(() => {
    if (isListening && recognitionRef.current) {
      recognitionRef.current.start();
    } else if (!isListening && recognitionRef.current) {
      recognitionRef.current.stop();
    }
  }, [isListening]);

  return (
    <button
      onClick={() => setIsListening(!isListening)}
      className={`p-6 rounded-full text-white text-2xl font-bold transition-all ${
        isListening ? 'bg-red-500 animate-pulse scale-110' : 'bg-green-600 hover:bg-green-700'
      }`}
    >
      {isListening ? '🎤 Listening...' : '🎙️ Speak'}
    </button>
  );
}