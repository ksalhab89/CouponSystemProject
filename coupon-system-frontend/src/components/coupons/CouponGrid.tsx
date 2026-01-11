import React from 'react';
import { Box, Typography, Grid } from '@mui/material';
import { Coupon } from '../../types/coupon.types';
import { CouponCard } from './CouponCard';
import { LoadingSpinner } from '../common/LoadingSpinner';

interface CouponGridProps {
  coupons: Coupon[];
  onPurchase?: (coupon: Coupon) => void;
  onEdit?: (coupon: Coupon) => void;
  onDelete?: (couponId: number) => void;
  showActions: boolean;
  loading: boolean;
}

export const CouponGrid: React.FC<CouponGridProps> = ({
  coupons,
  onPurchase,
  onEdit,
  onDelete,
  showActions,
  loading,
}) => {
  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <LoadingSpinner message="Loading coupons..." />
      </Box>
    );
  }

  if (coupons.length === 0) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '300px',
          backgroundColor: '#f5f5f5',
          borderRadius: 1,
        }}
      >
        <Typography variant="h6" color="textSecondary">
          No coupons found
        </Typography>
      </Box>
    );
  }

  return (
    <Grid container spacing={3}>
      {coupons.map((coupon) => (
        <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={coupon.id}>
          <CouponCard
            coupon={coupon}
            onPurchase={onPurchase ? () => onPurchase(coupon) : undefined}
            onEdit={onEdit ? () => onEdit(coupon) : undefined}
            onDelete={onDelete ? () => onDelete(coupon.id) : undefined}
            showActions={showActions}
          />
        </Grid>
      ))}
    </Grid>
  );
};
