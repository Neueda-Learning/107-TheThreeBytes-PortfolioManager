# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Environment Setup

Copy `frontend/.env.example` to `frontend/.env` and set values for your machine.

- `VITE_API_BASE_URL` - backend API base URL (example: `http://localhost:8081/api`)
- `VITE_MARKET_DEFAULT_ASSET_TYPE` - `STOCK`, `BOND`, or `CRYPTO`
- `VITE_MARKET_DEFAULT_TIMEFRAME` - `DAILY`, `WEEKLY`, or `MONTHLY`
- `VITE_MARKET_SUGGESTION_LIMIT` - max search suggestions shown

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
