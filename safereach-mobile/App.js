import React from 'react';
import { StyleSheet, SafeAreaView } from 'react-native';
import { WebView } from 'react-native-webview';
import { StatusBar } from 'expo-status-bar';

export default function App() {
  return (
    <SafeAreaView style={styles.container}>
      <StatusBar style="light" backgroundColor="#0b111e" />
      <WebView 
        source={{ uri: 'http://10.98.13.174:3000/' }} 
        style={styles.webview}
        domStorageEnabled={true}
        javaScriptEnabled={true}
        geolocationEnabled={true}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0b111e',
  },
  webview: {
    flex: 1,
  },
});
