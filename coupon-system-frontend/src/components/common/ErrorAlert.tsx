import React, { useState, useEffect } from 'react';
import { Snackbar, Alert, AlertProps } from '@mui/material';

interface ErrorAlertProps {
  message: string;
  severity: 'error' | 'warning' | 'info' | 'success';
  open: boolean;
  onClose: () => void;
}

export const ErrorAlert: React.FC<ErrorAlertProps> = ({
  message,
  severity,
  open,
  onClose,
}) => {
  const [isOpen, setIsOpen] = useState(open);

  useEffect(() => {
    setIsOpen(open);
  }, [open]);

  const handleClose = (
    event?: React.SyntheticEvent | Event,
    reason?: string
  ) => {
    if (reason === 'clickaway') {
      return;
    }
    setIsOpen(false);
    onClose();
  };

  return (
    <Snackbar
      open={isOpen}
      autoHideDuration={6000}
      onClose={handleClose}
      anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
    >
      <Alert
        onClose={handleClose}
        severity={severity as AlertProps['severity']}
        sx={{ width: '100%' }}
      >
        {message}
      </Alert>
    </Snackbar>
  );
};
