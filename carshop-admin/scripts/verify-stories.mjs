#!/usr/bin/env node
// USER_STORIES.md US-08/09/10/11 行为验证 · Playwright 实跑
//
// 用法:
//   node scripts/verify-stories.mjs                → mock 模式(MSW)
//   MODE=real ...                                   → 真后端模式
//
// 跑通后落 PNG 证据到 artifacts/06-admin-web-screenshots/<mode>/stories/
import { chromium } from '@playwright/test';
import { mkdirSync, existsSync, createReadStream, statSync, writeFileSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { tmpdir } from 'node:os';
import { writeFile } from 'node:fs/promises';

const __dirname = dirname(fileURLToPath(import.meta.url));
const BASE_URL = process.env.BASE_URL ?? 'http://127.0.0.1:5173';
const MODE = process.env.MODE ?? 'mock';
const OUT_DIR = resolve(__dirname, '../../artifacts/06-admin-web-screenshots', MODE, 'stories');
if (!existsSync(OUT_DIR)) mkdirSync(OUT_DIR, { recursive: true });

// 最小 PNG · 1x1 透明
const TINY_PNG = Buffer.from(
  '89504e470d0a1a0a0000000d49484452000000010000000108060000001f15c4890000000d49444154789c63000100000005000100' +
  '0d0a2db40000000049454e44ae426082',
  'hex',
);
// 6MB junk PNG header + tail
const BIG_PNG_PATH = resolve(tmpdir(), 'carshop-test-big.png');
if (!existsSync(BIG_PNG_PATH) || statSync(BIG_PNG_PATH).size < 6 * 1024 * 1024) {
  const buf = Buffer.concat([TINY_PNG, Buffer.alloc(6 * 1024 * 1024, 0)]);
  writeFileSync(BIG_PNG_PATH, buf);
}
const TINY_PNG_PATH = resolve(tmpdir(), 'carshop-test-tiny.png');
writeFileSync(TINY_PNG_PATH, TINY_PNG);
const BAD_EXE_PATH = resolve(tmpdir(), 'carshop-test-bad.exe');
writeFileSync(BAD_EXE_PATH, 'MZ-not-really-an-exe');

const results = [];
function log(story, msg, ok) {
  const tag = ok ? '✓' : '✗';
  console.log(`${tag} [${story}] ${msg}`);
  results.push({ story, msg, ok });
}

async function expectToast(page, regex, timeout = 3000) {
  // AntD message:.ant-message-notice
  const ok = await page
    .getByText(regex)
    .first()
    .waitFor({ timeout })
    .then(() => true)
    .catch(() => false);
  return ok;
}

(async () => {
  const browser = await chromium.launch();
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 }, locale: 'zh-CN' });
  const page = await ctx.newPage();

  // === US-08 场景 2:登录密码错 ===
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle' });
  await page.waitForSelector('input[type="password"]');
  await page.fill('input[id="username"]', 'admin');
  await page.fill('input[id="password"]', 'wrong-password');
  await page.click('button[type="submit"]');
  const wrongPwOk = await expectToast(page, /用户名或密码错误/);
  log('US-08.2', '密码错 → 文案 "用户名或密码错误"', wrongPwOk);
  await page.screenshot({ path: `${OUT_DIR}/us08-login-wrong-pw.png`, fullPage: true });

  // === US-08 场景 1:登录成功 ===
  await page.fill('input[id="password"]', 'admin123');
  await page.click('button[type="submit"]');
  await page.waitForURL(/\/products/, { timeout: 10000 });
  await page.waitForLoadState('networkidle');
  log('US-08.1', '正确密码 → 跳 /products', true);
  await page.screenshot({ path: `${OUT_DIR}/us08-login-success.png`, fullPage: true });

  // === US-10 场景 1:删有商品的分类 → 1002 ===
  await page.getByRole('menuitem', { name: '分类管理' }).click();
  await page.waitForSelector('.ant-table-row');
  // 找第一行(汽车用品,id=1,有商品)的"删除"按钮
  await page.locator('.ant-table-row').first().getByRole('button', { name: '删除' }).click();
  await page.getByRole('button', { name: '确 定' }).click();
  const conflictOk = await expectToast(page, /该分类下还有 \d+ 个商品/);
  log('US-10.1', '删有商品分类 → 1002 文案命中', conflictOk);
  await page.screenshot({ path: `${OUT_DIR}/us10-delete-conflict.png`, fullPage: true });
  // 等 toast 消散
  await page.waitForTimeout(2500);

  // === US-10 场景 2:新建空分类 → 删 → 成功 ===
  await page.getByRole('button', { name: /新建分类/ }).click();
  await page.locator('input[id="name"]').fill('测试空分类-自动化');
  await page.locator('input[id="icon_url"]').fill('https://picsum.photos/seed/test/32/32');
  await page.locator('input[id="sort"]').fill('999');
  await page.getByRole('button', { name: '确 定' }).click();
  const createOk = await expectToast(page, /已新建/);
  log('US-10.2a', '新建空分类 → 成功', createOk);
  await page.waitForTimeout(1500);

  // 删除新建的分类(最后一行)
  await page.locator('.ant-table-row').last().getByRole('button', { name: '删除' }).click();
  await page.getByRole('button', { name: '确 定' }).click();
  const deleteOk = await expectToast(page, /已删除/);
  log('US-10.2b', '删空分类 → 成功', deleteOk);
  await page.screenshot({ path: `${OUT_DIR}/us10-empty-delete.png`, fullPage: true });
  await page.waitForTimeout(1500);

  // === US-09 场景 2:上传 > 5MB ===
  await page.getByRole('menuitem', { name: '商品管理' }).click();
  await page.getByRole('button', { name: /新建商品/ }).click();
  await page.waitForSelector('form');
  // 上传超大文件
  await page.setInputFiles('input[type="file"]', BIG_PNG_PATH);
  const tooLargeOk = await expectToast(page, /不能超过 5MB/);
  log('US-09.2', '上传 > 5MB → 文案 "文件不能超过 5MB"', tooLargeOk);
  await page.screenshot({ path: `${OUT_DIR}/us09-upload-too-large.png`, fullPage: true });
  await page.waitForTimeout(1500);

  // === US-09 场景 3:上传非法格式 ===
  await page.setInputFiles('input[type="file"]', BAD_EXE_PATH);
  const wrongFmtOk = await expectToast(page, /格式不支持/);
  log('US-09.3', '上传 .exe → 文案 "格式不支持"', wrongFmtOk);
  await page.screenshot({ path: `${OUT_DIR}/us09-upload-wrong-fmt.png`, fullPage: true });
  await page.waitForTimeout(1500);

  // === US-09 场景 1:完整流程(上传 PNG + 填表 + 提交 + 在列表里看到) ===
  await page.setInputFiles('input[type="file"]', TINY_PNG_PATH);
  await expectToast(page, /上传成功/);
  await page.locator('input[id="title"]').fill('US-09 自动化测试商品');
  // 选分类:汽车用品(第一个) · AntD Select 把下拉渲染到 body portal
  await page.locator('input[id="category_id"]').click();
  await page.waitForSelector('.ant-select-item-option');
  await page.locator('.ant-select-item-option').first().click();
  // 类型:服务券
  await page.locator('input[type="radio"][value="service_voucher"]').click({ force: true });
  // 现价:¥12.34 · AntD InputNumber 的 input role="spinbutton"
  await page.locator('input#price, .ant-input-number-input').first().fill('12.34');
  await page.getByRole('button', { name: '保 存' }).click();
  await page.waitForURL(/\/products$/, { timeout: 10000 });
  await page.waitForSelector('.ant-table-row');
  // 找新建的商品
  const newProdVisible = await page
    .getByText('US-09 自动化测试商品')
    .first()
    .isVisible()
    .catch(() => false);
  log('US-09.1', '新建商品后在列表里看到', newProdVisible);
  // 验证价格 ¥12.34 显示
  const priceVisible = await page
    .getByText('¥12.34')
    .first()
    .isVisible()
    .catch(() => false);
  log('US-09.1', '新商品价格 ¥12.34(元→分→元 边界转换正确)', priceVisible);
  await page.screenshot({ path: `${OUT_DIR}/us09-create-success.png`, fullPage: true });

  // === US-11:订单列表 + device_id_short ===
  await page.getByRole('menuitem', { name: '订单查看' }).click();
  await page.waitForSelector('.ant-tabs-nav');
  const orderIdVisible = await page
    .locator('.ant-table-row')
    .first()
    .isVisible()
    .catch(() => false);
  const deviceShortVisible = await page
    .getByText(/ce-abc|…[a-z]{2}-[a-z]{3}/)
    .first()
    .isVisible()
    .catch(() => false);
  log('US-11', '订单列表有数据 + device_id_short 可见', orderIdVisible && deviceShortVisible);
  await page.screenshot({ path: `${OUT_DIR}/us11-orders-list.png`, fullPage: true });

  // === US-11:点订单打开 Drawer ===
  await page.locator('.ant-table-row').first().click();
  await page.waitForSelector('.ant-drawer-content');
  const drawerOk = await page
    .getByText(/订单详情/)
    .first()
    .isVisible()
    .catch(() => false);
  log('US-11', '订单详情 Drawer 可打开', drawerOk);
  await page.screenshot({ path: `${OUT_DIR}/us11-order-detail.png`, fullPage: true });

  await browser.close();

  // === 汇总 ===
  const ok = results.filter((r) => r.ok).length;
  const total = results.length;
  console.log(`\n=== ${MODE} 模式验证:${ok}/${total} 通过 ===`);
  results.filter((r) => !r.ok).forEach((r) => console.log(`✗ [${r.story}] ${r.msg}`));
  await writeFile(`${OUT_DIR}/results.json`, JSON.stringify({ mode: MODE, ok, total, results }, null, 2));
  process.exit(ok === total ? 0 : 1);
})().catch((e) => {
  console.error(e);
  process.exit(2);
});
