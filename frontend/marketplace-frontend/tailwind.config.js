/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          orange: '#F2680D',
          blue: '#004EAB',
          gray: '#717171'
        }
      }
    },
  },
  plugins: [],
}
