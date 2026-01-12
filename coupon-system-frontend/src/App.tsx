import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import muiTheme from './theme/muiTheme';
import { AuthProvider } from './contexts/AuthContext';
import { ProtectedRoute } from './components/common/ProtectedRoute';

// Pages
import HomePage from './pages/public/HomePage';
import LoginPage from './pages/public/LoginPage';
import CouponBrowsePage from './pages/public/CouponBrowsePage';
import CustomerDashboard from './pages/customer/CustomerDashboard';
import CompanyDashboard from './pages/company/CompanyDashboard';
import AdminDashboard from './pages/admin/AdminDashboard';
import ManageCompanies from './pages/admin/ManageCompanies';
import ManageCustomers from './pages/admin/ManageCustomers';
import MyCoupons from './pages/company/MyCoupons';
import CreateCoupon from './pages/company/CreateCoupon';
import EditCoupon from './pages/company/EditCoupon';
import BrowseCoupons from './pages/customer/BrowseCoupons';
import PurchasedCoupons from './pages/customer/PurchasedCoupons';

function App() {
  return (
    <ThemeProvider theme={muiTheme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            {/* Public routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/browse-coupons" element={<CouponBrowsePage />} />
            <Route path="/browse" element={<CouponBrowsePage />} />

            {/* Customer routes */}
            <Route
              path="/customer"
              element={
                <ProtectedRoute allowedRoles={['customer']}>
                  <CustomerDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/customer/browse"
              element={
                <ProtectedRoute allowedRoles={['customer']}>
                  <BrowseCoupons />
                </ProtectedRoute>
              }
            />
            <Route
              path="/customer/purchased"
              element={
                <ProtectedRoute allowedRoles={['customer']}>
                  <PurchasedCoupons />
                </ProtectedRoute>
              }
            />

            {/* Company routes */}
            <Route
              path="/company"
              element={
                <ProtectedRoute allowedRoles={['company']}>
                  <CompanyDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/company/coupons"
              element={
                <ProtectedRoute allowedRoles={['company']}>
                  <MyCoupons />
                </ProtectedRoute>
              }
            />
            <Route
              path="/company/create"
              element={
                <ProtectedRoute allowedRoles={['company']}>
                  <CreateCoupon />
                </ProtectedRoute>
              }
            />
            <Route
              path="/company/edit/:id"
              element={
                <ProtectedRoute allowedRoles={['company']}>
                  <EditCoupon />
                </ProtectedRoute>
              }
            />

            {/* Admin routes */}
            <Route
              path="/admin"
              element={
                <ProtectedRoute allowedRoles={['admin']}>
                  <AdminDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/companies"
              element={
                <ProtectedRoute allowedRoles={['admin']}>
                  <ManageCompanies />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/customers"
              element={
                <ProtectedRoute allowedRoles={['admin']}>
                  <ManageCustomers />
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
