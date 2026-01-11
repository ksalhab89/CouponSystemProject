import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  Button,
  IconButton,
  Drawer,
  List,
  ListItemButton,
  ListItemText,
  Box,
  Typography,
  useMediaQuery,
  useTheme,
  Divider,
  Menu,
  MenuItem,
  Avatar,
  Stack,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import CloseIcon from '@mui/icons-material/Close';
import LogoutIcon from '@mui/icons-material/Logout';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

interface NavLink {
  label: string;
  path: string;
}

interface NavConfig {
  [key: string]: NavLink[];
}

interface NavbarProps {
  title?: string;
}

const Navbar: React.FC<NavbarProps> = ({ title = 'CouponHub' }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const navigate = useNavigate();
  const { user, logout, isAuthenticated } = useAuth();

  const [drawerOpen, setDrawerOpen] = useState(false);
  const [profileMenuAnchorEl, setProfileMenuAnchorEl] = useState<null | HTMLElement>(null);
  const profileMenuOpen = Boolean(profileMenuAnchorEl);

  // Navigation links based on user role
  const navConfig: NavConfig = {
    public: [
      { label: 'Home', path: '/' },
      { label: 'Browse', path: '/browse' },
      { label: 'Login', path: '/login' },
    ],
    customer: [
      { label: 'Home', path: '/' },
      { label: 'Browse Coupons', path: '/browse-coupons' },
      { label: 'My Purchases', path: '/my-purchases' },
    ],
    company: [
      { label: 'My Coupons', path: '/my-coupons' },
      { label: 'Create Coupon', path: '/create-coupon' },
    ],
    admin: [
      { label: 'Dashboard', path: '/admin/dashboard' },
      { label: 'Companies', path: '/admin/companies' },
      { label: 'Customers', path: '/admin/customers' },
    ],
  };

  // Determine which navigation links to show
  const getNavLinks = (): NavLink[] => {
    if (!isAuthenticated || !user) {
      return navConfig.public;
    }

    if (user.clientType === 'admin') {
      return navConfig.admin;
    }

    if (user.clientType === 'company') {
      return navConfig.company;
    }

    if (user.clientType === 'customer') {
      return navConfig.customer;
    }

    return navConfig.public;
  };

  const navLinks = getNavLinks();

  const handleDrawerToggle = () => {
    setDrawerOpen(!drawerOpen);
  };

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setProfileMenuAnchorEl(event.currentTarget);
  };

  const handleProfileMenuClose = () => {
    setProfileMenuAnchorEl(null);
  };

  const handleNavigate = (path: string) => {
    navigate(path);
    setDrawerOpen(false);
  };

  const handleLogout = () => {
    logout();
    handleProfileMenuClose();
    navigate('/login');
  };

  // Extract initials from user name for avatar
  const getInitials = (name: string): string => {
    return name
      .split(' ')
      .map((word) => word[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  // Desktop Navigation Links
  const DesktopNavLinks: React.FC = () => (
    <>
      {navLinks.map((link) => (
        <Button
          key={link.path}
          color="inherit"
          onClick={() => handleNavigate(link.path)}
          sx={{
            textTransform: 'none',
            fontSize: '1rem',
            mx: 1,
            '&:hover': {
              backgroundColor: 'rgba(255, 255, 255, 0.1)',
              borderRadius: 1,
            },
          }}
        >
          {link.label}
        </Button>
      ))}
    </>
  );

  // Mobile Drawer Navigation
  const DrawerContent: React.FC = () => (
    <Box
      sx={{
        width: 280,
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
      }}
      role="presentation"
    >
      <Box sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
          Menu
        </Typography>
        <IconButton
          edge="end"
          color="inherit"
          onClick={handleDrawerToggle}
          aria-label="close menu"
        >
          <CloseIcon />
        </IconButton>
      </Box>

      <Divider />

      <List sx={{ flex: 1 }}>
        {navLinks.map((link) => (
          <ListItemButton
            key={link.path}
            onClick={() => handleNavigate(link.path)}
            sx={{
              py: 1.5,
              '&:hover': {
                backgroundColor: 'action.hover',
              },
            }}
          >
            <ListItemText
              primary={link.label}
              primaryTypographyProps={{
                sx: { fontWeight: 500 },
              }}
            />
          </ListItemButton>
        ))}
      </List>

      {isAuthenticated && user && (
        <>
          <Divider />
          <Box sx={{ p: 2 }}>
            <Typography variant="body2" color="textSecondary" sx={{ mb: 1 }}>
              Email: {user.email}
            </Typography>
            <Button
              fullWidth
              variant="outlined"
              color="error"
              startIcon={<LogoutIcon />}
              onClick={handleLogout}
              sx={{ textTransform: 'none' }}
            >
              Logout
            </Button>
          </Box>
        </>
      )}
    </Box>
  );

  return (
    <AppBar
      position="sticky"
      sx={{
        backgroundColor: theme.palette.primary.main,
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)',
      }}
    >
      <Toolbar
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        {/* Logo/Brand */}
        <Box
          onClick={() => handleNavigate('/')}
          sx={{
            display: 'flex',
            alignItems: 'center',
            cursor: 'pointer',
            textDecoration: 'none',
            color: 'white',
            fontWeight: 'bold',
            fontSize: '1.5rem',
            '&:hover': {
              opacity: 0.8,
            },
          }}
        >
          {title}
        </Box>

        {/* Desktop Navigation */}
        {!isMobile && (
          <Box sx={{ display: 'flex', alignItems: 'center', flex: 1, justifyContent: 'center' }}>
            <DesktopNavLinks />
          </Box>
        )}

        {/* Right side: User profile or mobile menu */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {/* User Profile Section (Desktop) */}
          {!isMobile && isAuthenticated && user && (
            <Stack
              direction="row"
              spacing={1}
              alignItems="center"
              sx={{
                cursor: 'pointer',
                padding: '8px 12px',
                borderRadius: 1,
                '&:hover': {
                  backgroundColor: 'rgba(255, 255, 255, 0.1)',
                },
              }}
              onClick={handleProfileMenuOpen}
            >
              <Avatar
                sx={{
                  width: 32,
                  height: 32,
                  backgroundColor: theme.palette.secondary.main,
                  fontSize: '0.875rem',
                  fontWeight: 'bold',
                }}
              >
                {getInitials(user.name)}
              </Avatar>
              <Box>
                <Typography
                  variant="body2"
                  sx={{
                    color: 'white',
                    fontWeight: 500,
                  }}
                >
                  {user.name}
                </Typography>
                <Typography
                  variant="caption"
                  sx={{
                    color: 'rgba(255, 255, 255, 0.7)',
                    display: 'block',
                    maxWidth: 150,
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                  }}
                >
                  {user.email}
                </Typography>
              </Box>
            </Stack>
          )}

          {/* Profile Menu (Desktop) */}
          {!isMobile && isAuthenticated && user && (
            <Menu
              anchorEl={profileMenuAnchorEl}
              open={profileMenuOpen}
              onClose={handleProfileMenuClose}
              anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'right',
              }}
              transformOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
            >
              <MenuItem disabled>
                <Box>
                  <Typography variant="body2">{user.name}</Typography>
                  <Typography variant="caption" color="textSecondary">
                    {user.email}
                  </Typography>
                </Box>
              </MenuItem>
              <Divider />
              <MenuItem onClick={handleLogout}>
                <LogoutIcon sx={{ mr: 1, fontSize: '1.25rem' }} />
                Logout
              </MenuItem>
            </Menu>
          )}

          {/* Mobile Menu Toggle */}
          {isMobile && (
            <IconButton
              color="inherit"
              edge="end"
              onClick={handleDrawerToggle}
              aria-label="open navigation menu"
              sx={{ ml: 2 }}
            >
              <MenuIcon />
            </IconButton>
          )}
        </Box>
      </Toolbar>

      {/* Mobile Drawer */}
      <Drawer
        anchor="right"
        open={drawerOpen}
        onClose={handleDrawerToggle}
        PaperProps={{
          sx: {
            backgroundColor: theme.palette.background.paper,
          },
        }}
      >
        <DrawerContent />
      </Drawer>
    </AppBar>
  );
};

export { Navbar };
export default Navbar;
