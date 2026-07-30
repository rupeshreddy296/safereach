/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        deepBlue: '#003366',
        accentRed: '#C62828',
        lightBg: '#F5F7FA',
        darkBg: '#0b111e',
        darkCard: 'rgba(18, 25, 41, 0.7)',
        borderGlass: 'rgba(255, 255, 255, 0.08)',
      },
      fontFamily: {
        heading: ['Outfit', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
