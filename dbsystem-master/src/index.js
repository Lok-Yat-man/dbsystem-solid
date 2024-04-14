import React from 'react';
import App from './app';

const title = 'Covid-19 demo';

import { createRoot } from 'react-dom/client';
const container = document.getElementById('app');
const root = createRoot(container); // createRoot(container!) if you use TypeScript
root.render(<App title={title} />)