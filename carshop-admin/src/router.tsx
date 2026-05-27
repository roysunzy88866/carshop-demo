import { createBrowserRouter, Navigate } from 'react-router-dom';
import AppLayout from './pages/Layout';
import LoginPage from './pages/Login';
import CategoryList from './pages/CategoryList';
import ProductList from './pages/ProductList';
import ProductEdit from './pages/ProductEdit';
import BannerList from './pages/BannerList';
import OrderList from './pages/OrderList';

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to="/products" replace /> },
      { path: 'categories', element: <CategoryList /> },
      { path: 'products', element: <ProductList /> },
      { path: 'products/new', element: <ProductEdit /> },
      { path: 'products/:id/edit', element: <ProductEdit /> },
      { path: 'banners', element: <BannerList /> },
      { path: 'orders', element: <OrderList /> },
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
]);
