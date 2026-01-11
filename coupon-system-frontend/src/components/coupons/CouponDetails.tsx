import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Chip,
  Divider,
  Card,
  CardMedia,
  Stack,
  IconButton,
  CircularProgress,
  Grid,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import StorefrontIcon from '@mui/icons-material/Storefront';
import { Coupon } from '../../types/coupon.types';
import { getCategoryName } from '../../utils/categoryHelper';

interface CouponDetailsProps {
  coupon: Coupon | null;
  open: boolean;
  onClose: () => void;
  onPurchase?: (couponId: number) => Promise<void> | void;
}

export const CouponDetails: React.FC<CouponDetailsProps> = ({
  coupon,
  open,
  onClose,
  onPurchase,
}) => {
  const [isLoading, setIsLoading] = useState(false);

  const handlePurchase = async () => {
    if (!coupon || !onPurchase) return;

    try {
      setIsLoading(true);
      await onPurchase(coupon.id);
      onClose();
    } catch (error) {
      console.error('Failed to purchase coupon:', error);
      setIsLoading(false);
    }
  };

  const formatDate = (dateString: string): string => {
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      });
    } catch {
      return dateString;
    }
  };

  const isExpired = coupon ? new Date(coupon.endDate) < new Date() : false;
  const isOutOfStock = coupon ? coupon.amount === 0 : false;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
        },
      }}
    >
      {/* Dialog Header with Close Button */}
      <DialogTitle
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          paddingBottom: 1,
        }}
      >
        <Typography variant="h5" sx={{ fontWeight: 600 }}>
          Coupon Details
        </Typography>
        <IconButton
          onClick={onClose}
          size="small"
          sx={{
            color: 'text.secondary',
            '&:hover': {
              color: 'text.primary',
            },
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <Divider />

      {/* Dialog Content */}
      <DialogContent sx={{ paddingTop: 3 }}>
        {coupon ? (
          <Stack spacing={3}>
            {/* Coupon Image */}
            <Card
              sx={{
                borderRadius: 2,
                overflow: 'hidden',
                boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
              }}
            >
              <CardMedia
                component="img"
                image={coupon.image}
                alt={coupon.title}
                sx={{
                  height: 280,
                  objectFit: 'cover',
                }}
              />
            </Card>

            {/* Title */}
            <Typography
              variant="h5"
              sx={{
                fontWeight: 700,
                color: 'text.primary',
              }}
            >
              {coupon.title}
            </Typography>

            {/* Status Chips */}
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
              {isExpired && (
                <Chip
                  label="Expired"
                  color="error"
                  size="small"
                  variant="outlined"
                />
              )}
              {isOutOfStock && (
                <Chip
                  label="Out of Stock"
                  color="error"
                  size="small"
                  variant="outlined"
                />
              )}
              <Chip
                icon={<LocalOfferIcon />}
                label={getCategoryName(coupon.CATEGORY)}
                color="primary"
                variant="outlined"
                size="small"
              />
            </Box>

            {/* Price and Availability Section */}
            <Grid container spacing={2}>
              <Grid size={6}>
                <Box>
                  <Typography
                    variant="caption"
                    sx={{
                      display: 'block',
                      color: 'text.secondary',
                      marginBottom: 0.5,
                      fontWeight: 500,
                    }}
                  >
                    Price
                  </Typography>
                  <Typography
                    variant="h6"
                    sx={{
                      fontWeight: 700,
                      color: 'success.main',
                    }}
                  >
                    ${coupon.price.toFixed(2)}
                  </Typography>
                </Box>
              </Grid>
              <Grid size={6}>
                <Box>
                  <Typography
                    variant="caption"
                    sx={{
                      display: 'block',
                      color: 'text.secondary',
                      marginBottom: 0.5,
                      fontWeight: 500,
                    }}
                  >
                    Available
                  </Typography>
                  <Typography
                    variant="h6"
                    sx={{
                      fontWeight: 700,
                      color: coupon.amount > 0 ? 'primary.main' : 'error.main',
                    }}
                  >
                    {coupon.amount} {coupon.amount === 1 ? 'item' : 'items'}
                  </Typography>
                </Box>
              </Grid>
            </Grid>

            <Divider sx={{ my: 1 }} />

            {/* Description */}
            <Box>
              <Typography
                variant="caption"
                sx={{
                  display: 'block',
                  color: 'text.secondary',
                  marginBottom: 1,
                  fontWeight: 500,
                  textTransform: 'uppercase',
                  letterSpacing: 0.5,
                }}
              >
                Description
              </Typography>
              <Typography
                variant="body2"
                sx={{
                  color: 'text.primary',
                  lineHeight: 1.6,
                }}
              >
                {coupon.description}
              </Typography>
            </Box>

            {/* Details Grid */}
            <Grid container spacing={2}>
              {/* Start Date */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <Box
                  sx={{
                    padding: 1.5,
                    backgroundColor: 'background.default',
                    borderRadius: 1,
                  }}
                >
                  <Typography
                    variant="caption"
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 0.75,
                      color: 'text.secondary',
                      marginBottom: 0.5,
                      fontWeight: 500,
                    }}
                  >
                    <CalendarMonthIcon sx={{ fontSize: 16 }} />
                    Start Date
                  </Typography>
                  <Typography
                    variant="body2"
                    sx={{
                      fontWeight: 600,
                      color: 'text.primary',
                    }}
                  >
                    {formatDate(coupon.startDate)}
                  </Typography>
                </Box>
              </Grid>

              {/* End Date */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <Box
                  sx={{
                    padding: 1.5,
                    backgroundColor: 'background.default',
                    borderRadius: 1,
                    borderLeft:
                      isExpired ? '4px solid' : 'none',
                    borderColor: 'error.main',
                  }}
                >
                  <Typography
                    variant="caption"
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 0.75,
                      color: isExpired ? 'error.main' : 'text.secondary',
                      marginBottom: 0.5,
                      fontWeight: 500,
                    }}
                  >
                    <CalendarMonthIcon sx={{ fontSize: 16 }} />
                    Expiry Date
                  </Typography>
                  <Typography
                    variant="body2"
                    sx={{
                      fontWeight: 600,
                      color: isExpired ? 'error.main' : 'text.primary',
                    }}
                  >
                    {formatDate(coupon.endDate)}
                  </Typography>
                </Box>
              </Grid>

              {/* Company ID */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <Box
                  sx={{
                    padding: 1.5,
                    backgroundColor: 'background.default',
                    borderRadius: 1,
                  }}
                >
                  <Typography
                    variant="caption"
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 0.75,
                      color: 'text.secondary',
                      marginBottom: 0.5,
                      fontWeight: 500,
                    }}
                  >
                    <StorefrontIcon sx={{ fontSize: 16 }} />
                    Company ID
                  </Typography>
                  <Typography
                    variant="body2"
                    sx={{
                      fontWeight: 600,
                      color: 'text.primary',
                    }}
                  >
                    #{coupon.companyID}
                  </Typography>
                </Box>
              </Grid>

              {/* Coupon ID */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <Box
                  sx={{
                    padding: 1.5,
                    backgroundColor: 'background.default',
                    borderRadius: 1,
                  }}
                >
                  <Typography
                    variant="caption"
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 0.75,
                      color: 'text.secondary',
                      marginBottom: 0.5,
                      fontWeight: 500,
                    }}
                  >
                    <LocalOfferIcon sx={{ fontSize: 16 }} />
                    Coupon ID
                  </Typography>
                  <Typography
                    variant="body2"
                    sx={{
                      fontWeight: 600,
                      color: 'text.primary',
                    }}
                  >
                    #{coupon.id}
                  </Typography>
                </Box>
              </Grid>
            </Grid>
          </Stack>
        ) : (
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              minHeight: 300,
            }}
          >
            <Typography color="text.secondary">
              No coupon details available
            </Typography>
          </Box>
        )}
      </DialogContent>

      {/* Dialog Actions */}
      <Divider />
      <DialogActions
        sx={{
          padding: 2,
          gap: 1,
          justifyContent: 'flex-end',
        }}
      >
        <Button
          onClick={onClose}
          variant="outlined"
          color="inherit"
          sx={{
            borderRadius: 1,
          }}
        >
          Close
        </Button>

        {coupon && onPurchase && (
          <Button
            onClick={handlePurchase}
            variant="contained"
            color="primary"
            disabled={isLoading || isExpired || isOutOfStock}
            startIcon={
              isLoading ? (
                <CircularProgress size={20} color="inherit" />
              ) : (
                <ShoppingCartIcon />
              )
            }
            sx={{
              borderRadius: 1,
              minWidth: 140,
            }}
          >
            {isLoading ? 'Purchasing...' : 'Purchase'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default CouponDetails;
