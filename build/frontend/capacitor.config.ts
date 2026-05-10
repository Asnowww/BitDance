import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.bitdance.app',
  appName: 'BitDance',
  webDir: 'dist',
  bundledWebRuntime: false,
  android: {
    backgroundColor: '#FFFFFF',
    allowMixedContent: false
  },
  server: {
    androidScheme: 'https'
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 1200,
      backgroundColor: '#FF2442',
      androidScaleType: 'CENTER_CROP',
      showSpinner: false
    },
    StatusBar: {
      style: 'LIGHT',
      backgroundColor: '#FF2442'
    }
  }
};

export default config;
