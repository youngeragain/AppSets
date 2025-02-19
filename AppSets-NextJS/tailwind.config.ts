import type { Config } from 'tailwindcss'

const config: Config = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      width: {
        'px-0.5': '0.5px',
        'px-2': '2px',
        'px-3': '3px',
        'px-4': '4px',
        'px-5': '5px',
        "120":"30rem",
        "150":"37.5rem",
        "210":"52.5rem",
        "240":"60rem"
      },
      height: {
        '240': '60rem',
        "150":"37.5rem",
        '210': '52.5rem',
        '120': '30rem'
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-conic':
          'conic-gradient(from 180deg at 50% 50%, var(--tw-gradient-stops))',
      },
    },
  },
  plugins: [],
}
export default config
