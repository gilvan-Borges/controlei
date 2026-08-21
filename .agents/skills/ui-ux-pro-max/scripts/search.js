#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const SKILL_DIR = path.resolve(__dirname, '..');
const DATA_DIR = path.join(SKILL_DIR, 'data');
const STACKS_DIR = path.join(DATA_DIR, 'stacks');

const CSV_DOMAINS = {
  style: 'styles.csv',
  color: 'colors.csv',
  chart: 'charts.csv',
  landing: 'landing.csv',
  product: 'products.csv',
  ux: 'ux-guidelines.csv',
  typography: 'typography.csv',
  'google-fonts': 'google-fonts.csv',
  icons: 'icons.csv',
  motion: 'motion.csv',
  gsap: 'motion.csv',
  reasoning: 'ui-reasoning.csv',
  'react-perf': 'react-performance.csv',
  app: 'app-interface.csv'
};

function parseCSV(content) {
  const lines = [];
  let currentLine = [];
  let currentCell = '';
  let inQuotes = false;

  for (let i = 0; i < content.length; i++) {
    const char = content[i];
    const nextChar = content[i + 1];

    if (char === '"') {
      if (inQuotes && nextChar === '"') {
        currentCell += '"';
        i++;
      } else {
        inQuotes = !inQuotes;
      }
    } else if (char === ',' && !inQuotes) {
      currentLine.push(currentCell.trim());
      currentCell = '';
    } else if ((char === '\r' || char === '\n') && !inQuotes) {
      if (char === '\r' && nextChar === '\n') {
        i++;
      }
      currentLine.push(currentCell.trim());
      if (currentLine.some(c => c.length > 0)) {
        lines.push(currentLine);
      }
      currentLine = [];
      currentCell = '';
    } else {
      currentCell += char;
    }
  }
  if (currentCell.length > 0 || currentLine.length > 0) {
    currentLine.push(currentCell.trim());
    if (currentLine.some(c => c.length > 0)) {
      lines.push(currentLine);
    }
  }

  if (lines.length === 0) return [];
  const headers = lines[0];
  const rows = [];

  for (let i = 1; i < lines.length; i++) {
    const row = {};
    const values = lines[i];
    for (let j = 0; j < headers.length; j++) {
      row[headers[j]] = values[j] || '';
    }
    rows.push(row);
  }
  return rows;
}

function searchRows(rows, query, maxResults = 5) {
  const terms = query.toLowerCase().split(/\s+/).filter(Boolean);
  if (terms.length === 0) return rows.slice(0, maxResults);

  const scored = rows.map(row => {
    const text = Object.values(row).join(' ').toLowerCase();
    let score = 0;
    for (const term of terms) {
      if (text.includes(term)) {
        score += 1;
        const regex = new RegExp(`\\b${term}\\b`, 'i');
        if (regex.test(text)) score += 2;
      }
    }
    return { row, score };
  });

  const matched = scored.filter(item => item.score > 0);
  if (matched.length === 0) {
    // Return top default rows if no exact keyword match
    return rows.slice(0, maxResults);
  }

  return matched
    .sort((a, b) => b.score - a.score)
    .slice(0, maxResults)
    .map(item => item.row);
}

function runCLI() {
  const args = process.argv.slice(2);
  if (args.length === 0 || args.includes('--help') || args.includes('-h')) {
    console.log(`
UI/UX Pro Max Search Engine (Node.js)
Usage:
  node search.js "<query>" [--domain <domain>] [--stack <stack>] [--max-results <n>] [--json]
  node search.js "<query>" --design-system [-p "Project Name"]

Domains: ${Object.keys(CSV_DOMAINS).join(', ')}
Available Stacks: angular, react, nextjs, vue, svelte, astro, html-tailwind, shadcn, flutter, swiftui, etc.
    `);
    process.exit(0);
  }

  let query = '';
  let domain = null;
  let stack = null;
  let maxResults = 4;
  let isJson = false;
  let isDesignSystem = false;
  let projectName = 'Project';

  for (let i = 0; i < args.length; i++) {
    const arg = args[i];
    if (arg === '--domain' || arg === '-d') {
      domain = args[++i];
    } else if (arg === '--stack' || arg === '-s') {
      stack = args[++i];
    } else if (arg === '--max-results' || arg === '-n') {
      maxResults = parseInt(args[++i], 10) || 4;
    } else if (arg === '--json') {
      isJson = true;
    } else if (arg === '--design-system' || arg === '-ds') {
      isDesignSystem = true;
    } else if (arg === '--project-name' || arg === '-p') {
      projectName = args[++i];
    } else if (!arg.startsWith('-')) {
      query += (query ? ' ' : '') + arg;
    }
  }

  if (isDesignSystem) {
    const stylesFile = path.join(DATA_DIR, CSV_DOMAINS.style);
    const colorsFile = path.join(DATA_DIR, CSV_DOMAINS.color);
    const typoFile = path.join(DATA_DIR, CSV_DOMAINS.typography);
    const uxFile = path.join(DATA_DIR, CSV_DOMAINS.ux);

    const styles = fs.existsSync(stylesFile) ? searchRows(parseCSV(fs.readFileSync(stylesFile, 'utf8')), query, 2) : [];
    const colors = fs.existsSync(colorsFile) ? searchRows(parseCSV(fs.readFileSync(colorsFile, 'utf8')), query, 2) : [];
    const typography = fs.existsSync(typoFile) ? searchRows(parseCSV(fs.readFileSync(typoFile, 'utf8')), query, 2) : [];
    const ux = fs.existsSync(uxFile) ? searchRows(parseCSV(fs.readFileSync(uxFile, 'utf8')), query, 4) : [];

    const result = {
      project: projectName,
      query,
      recommended_styles: styles,
      recommended_colors: colors,
      recommended_typography: typography,
      key_ux_guidelines: ux
    };

    if (isJson) {
      console.log(JSON.stringify(result, null, 2));
    } else {
      console.log(`\n============================================================`);
      console.log(`💎 DESIGN SYSTEM RECOMMENDATION: ${projectName.toUpperCase()}`);
      console.log(`🎯 Context Query: "${query}"`);
      console.log(`============================================================\n`);

      console.log(`### 🎨 Recommended Visual Styles:`);
      styles.forEach(s => {
        console.log(`• **${s['Style Category'] || s['Style ID'] || 'Style'}** (${s['Type'] || 'General'})`);
        console.log(`  - Keywords: ${s['Keywords'] || 'N/A'}`);
        console.log(`  - Primary Colors: ${s['Primary Colors'] || 'N/A'}`);
        console.log(`  - Best For: ${s['Best For'] || 'N/A'}`);
        console.log(`  - Effects: ${s['Effects & Animation'] || 'N/A'}`);
      });

      console.log(`\n### 🌈 Recommended Color Palettes:`);
      colors.forEach(c => {
        console.log(`• **${c['Product Type'] || 'Palette'}**`);
        console.log(`  - Primary: \x1b[36m${c['Primary']}\x1b[0m | Secondary: \x1b[35m${c['Secondary']}\x1b[0m | Accent: \x1b[33m${c['Accent']}\x1b[0m`);
        console.log(`  - Background: ${c['Background']} | Foreground: ${c['Foreground']} | Card: ${c['Card']}`);
        console.log(`  - Notes: ${c['Notes'] || 'N/A'}`);
      });

      console.log(`\n### 🔤 Recommended Typography Pairings:`);
      typography.forEach(t => {
        console.log(`• **${t['Font Pairing Name'] || 'Pairing'}** [${t['Category'] || 'Sans'}]`);
        console.log(`  - Heading Font: \x1b[1m${t['Heading Font']}\x1b[0m | Body Font: \x1b[1m${t['Body Font']}\x1b[0m`);
        console.log(`  - Best For: ${t['Best For'] || 'N/A'}`);
        console.log(`  - Import: ${t['Google Fonts URL'] || 'N/A'}`);
      });

      console.log(`\n### ⚡ Essential UX & Accessibility Guidelines:`);
      ux.forEach(u => {
        console.log(`• [${u['Category'] || 'UX'}] \x1b[32m${u['Issue'] || 'Rule'}\x1b[0m (Severity: ${u['Severity'] || 'Medium'})`);
        console.log(`  - Description: ${u['Description']}`);
        console.log(`  - ✅ DO: ${u['Do']}`);
        console.log(`  - ❌ DON'T: ${u['Don\'t']}`);
      });
      console.log(`\n============================================================\n`);
    }
    return;
  }

  let filePath = '';
  let activeDomain = domain || 'ux';

  if (stack) {
    filePath = path.join(STACKS_DIR, `${stack.toLowerCase()}.csv`);
    if (!fs.existsSync(filePath)) {
      console.error(`Stack '${stack}' not found. Available in: ${STACKS_DIR}`);
      process.exit(1);
    }
    activeDomain = `stack:${stack}`;
  } else {
    const filename = CSV_DOMAINS[domain] || CSV_DOMAINS.ux;
    filePath = path.join(DATA_DIR, filename);
  }

  if (!fs.existsSync(filePath)) {
    console.error(`File not found: ${filePath}`);
    process.exit(1);
  }

  const content = fs.readFileSync(filePath, 'utf8');
  const rows = parseCSV(content);
  const results = searchRows(rows, query, maxResults);

  if (isJson) {
    console.log(JSON.stringify({ domain: activeDomain, query, count: results.length, results }, null, 2));
  } else {
    console.log(`\n## UI/UX Pro Max Results`);
    console.log(`**Domain / Source:** ${activeDomain} | **Query:** "${query}" | **Found:** ${results.length}\n`);

    if (results.length === 0) {
      console.log(`No direct matches found for "${query}". Try broader search terms.`);
    } else {
      results.forEach((row, idx) => {
        console.log(`### Result ${idx + 1}`);
        for (const [k, v] of Object.entries(row)) {
          if (v) console.log(`- **${k}:** ${v}`);
        }
        console.log('');
      });
    }
  }
}

runCLI();
