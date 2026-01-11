import React from 'react';
import {
  ToggleButton,
  ToggleButtonGroup,
  Box,
  Typography,
} from '@mui/material';
import {
  AdminPanelSettings,
  Business,
  Person,
} from '@mui/icons-material';
import { ClientType } from '../../types/auth.types';

interface RoleSelectorProps {
  value: ClientType;
  onChange: (clientType: ClientType) => void;
}

export const RoleSelector: React.FC<RoleSelectorProps> = ({
  value,
  onChange,
}) => {
  const handleChange = (
    event: React.MouseEvent<HTMLElement>,
    newValue: ClientType | null
  ) => {
    if (newValue !== null) {
      onChange(newValue);
    }
  };

  const roles: Array<{ type: ClientType; label: string; icon: React.ReactNode }> = [
    { type: 'admin', label: 'Admin', icon: <AdminPanelSettings /> },
    { type: 'company', label: 'Company', icon: <Business /> },
    { type: 'customer', label: 'Customer', icon: <Person /> },
  ];

  return (
    <Box sx={{ width: '100%' }}>
      <Typography
        variant="subtitle2"
        sx={{
          mb: 1.5,
          color: 'text.secondary',
          fontWeight: 500,
        }}
      >
        Role
      </Typography>
      <ToggleButtonGroup
        value={value}
        exclusive
        onChange={handleChange}
        fullWidth
        sx={{
          display: 'flex',
          gap: 0,
          '& .MuiToggleButton-root': {
            flex: 1,
            borderRadius: '8px',
            border: '1px solid',
            borderColor: 'divider',
            textTransform: 'capitalize',
            py: 1.5,
            px: 1,
            transition: 'all 0.2s ease-in-out',
            '&:not(:last-of-type)': {
              mr: 1,
            },
          },
          '& .MuiToggleButton-root.Mui-selected': {
            backgroundColor: 'primary.main',
            color: '#fff',
            borderColor: 'primary.main',
            '&:hover': {
              backgroundColor: 'primary.dark',
              borderColor: 'primary.dark',
            },
          },
          '& .MuiToggleButton-root:not(.Mui-selected)': {
            backgroundColor: '#fff',
            color: 'text.primary',
            '&:hover': {
              backgroundColor: 'action.hover',
              borderColor: 'primary.light',
            },
          },
        }}
      >
        {roles.map((role) => (
          <ToggleButton
            key={role.type}
            value={role.type}
            aria-label={role.label}
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 0.75,
              '& svg': {
                fontSize: '1.5rem',
              },
            }}
          >
            {role.icon}
            <span style={{ fontSize: '0.875rem' }}>{role.label}</span>
          </ToggleButton>
        ))}
      </ToggleButtonGroup>
    </Box>
  );
};
