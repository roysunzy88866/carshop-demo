#!/usr/bin/env node
// Playwright 自动截图 · 06 admin-web 每个页面一张
//
// 用法:
//   node scripts/screenshot.mjs                    → mock 模式,截 6 张到 artifacts/06-admin-web-screenshots/mock/
//   MODE=real node scripts/screenshot.mjs          → 真后端模式,落到 real/
//   BASE_URL=http://localhost:5173 ...             → 自定义 base url
//
// 前置:dev server 已起在 BASE_URL(本脚本不负责拉起来),admin 后台账号 admin/admin123 可登录
import { chromium } from '@playwright/test';
import { mkdirSync, existsSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const BASE_URL = process.env.BASE_URL ?? 'http://127.0.0.1:5173';
const MODE = process.env.MODE ?? 'mock'; // mock | real,只用于子目录命名
const OUT_DIR = resolve(__dirname, '../../artifacts/06-admin-web-screenshots', MODE);

if (!existsSync(OUT_DIR)) mkdirSync(OUT_DIR, { recursive: true });

// Sider 菜单 label → 截图 name + 截前等的选择器
const PAGES = [
  { name: '02-categories', menu: '分类管理', waitFor: '.ant-table-row, .ant-empty' },
  { name: '03-products', menu: '商品管理', waitFor: '.ant-table-row, .ant-empty' },
  { name: '05-banners', menu: 'Banner 管理', waitFor: '.ant-table-row, .ant-empty' },
  { name: '06-orders', menu: '订单查看', waitFor: '.ant-tabs-nav' },
];

(async () => {
  const browser = await chromium.launch();
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 }, locale: 'zh-CN' });
  const page = await ctx.newPage();
  page.on('pageerror', (e) => console.log('  [pageerror]', e.message));

  // 1. 登录页(未登录态)
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle' });
  await page.waitForSelector('input[type="password"]');
  await page.waitForTimeout(500);
  await page.screenshot({ path: `${OUT_DIR}/01-login.png`, fullPage: true });
  console.log('✓ 01-login.png');

  // 2. 登录
  await page.fill('input[id="username"]', 'admin');
  await page.fill('input[id="password"]', 'admin123');
  await page.click('button[type="submit"]');
  await page.waitForURL(/\/products/, { timeout: 10000 });
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(500);

  // 3. 逐页通过点 Sider 菜单(SPA 导航)
  for (const p of PAGES) {
    // 用 menu label 找菜单项点击
    await page.getByRole('menuitem', { name: p.menu }).click();
    try {
      await page.waitForSelector(p.waitFor, { timeout: 5000 });
    } catch {
      console.warn(`  ⚠ ${p.name} 等不到 ${p.waitFor},继续`);
    }
    await page.waitForTimeout(500);
    await page.screenshot({ path: `${OUT_DIR}/${p.name}.png`, fullPage: true });
    console.log(`✓ ${p.name}.png`);
  }

  // 4. 新建商品页(从商品列表点新建)
  await page.getByRole('menuitem', { name: '商品管理' }).click();
  await page.waitForLoadState('networkidle');
  await page.getByRole('button', { name: /新建商品/ }).click();
  await page.waitForURL(/\/products\/new/);
  await page.waitForSelector('form');
  await page.waitForTimeout(500);
  await page.screenshot({ path: `${OUT_DIR}/04-product-new.png`, fullPage: true });
  console.log('✓ 04-product-new.png');

  await browser.close();
  console.log(`\n截图已落到:${OUT_DIR}`);
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
