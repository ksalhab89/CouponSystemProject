import React from 'react';
import { Box, Container, Typography, Stack } from '@mui/material';

interface FooterProps {
  /**
   * Copyright holder name
   * @default "Coupon System"
   */
  copyrightText?: string;
}

export const Footer: React.FC<FooterProps> = ({
  copyrightText = 'Coupon System',
}) => {
  const currentYear = new Date().getFullYear();

  return (
    <Box
      component="footer"
      sx={{
        marginTop: 'auto',
      }}
    >
      <Box
        sx={{
          backgroundColor: '#f5f5f5', // Gray background
          borderTop: '1px solid #e0e0e0',
          py: 4,
        }}
      >
        <Container maxWidth="lg">
          <Stack spacing={2} alignItems="center">
            {/* Copyright Text */}
            <Typography
              variant="body2"
              color="textSecondary"
              sx={{
                textAlign: 'center',
                fontSize: '0.875rem',
              }}
            >
              © {currentYear} {copyrightText}. All rights reserved.
            </Typography>
          </Stack>
        </Container>
      </Box>
    </Box>
  );
};

export default Footer;
