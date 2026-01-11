import React from 'react';
import {
  Container,
  Box,
  Typography,
  AppBar,
  Toolbar,
  Link,
  CssBaseline,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { LoginForm } from '../../components/auth/LoginForm';
import Logo from '../../logo.svg';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      }}
    >
      <CssBaseline />

      {/* Navbar */}
      <AppBar position="static" elevation={0} sx={{ backgroundColor: 'rgba(0, 0, 0, 0.1)' }}>
        <Toolbar>
          <Box
            component="img"
            src={Logo}
            alt="Coupon System Logo"
            sx={{ height: 40, marginRight: 2, cursor: 'pointer' }}
            onClick={() => navigate('/')}
          />
          <Typography
            variant="h6"
            sx={{ flexGrow: 1, fontWeight: 600, cursor: 'pointer' }}
            onClick={() => navigate('/')}
          >
            Coupon System
          </Typography>
          <Link
            href="/"
            sx={{
              color: 'white',
              textDecoration: 'none',
              marginRight: 2,
              fontSize: '1rem',
              '&:hover': {
                textDecoration: 'underline',
              },
            }}
          >
            Home
          </Link>
        </Toolbar>
      </AppBar>

      {/* Main Content */}
      <Box
        sx={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          padding: { xs: 2, sm: 3, md: 4 },
        }}
      >
        <Container maxWidth="sm">
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 3,
            }}
          >
            {/* Page Title */}
            <Typography
              variant="h3"
              component="h1"
              sx={{
                fontWeight: 700,
                color: 'white',
                textAlign: 'center',
                textShadow: '0 2px 4px rgba(0, 0, 0, 0.2)',
              }}
            >
              Login to Coupon System
            </Typography>

            {/* LoginForm Component */}
            <LoginForm />
          </Box>
        </Container>
      </Box>
    </Box>
  );
};

export default LoginPage;
