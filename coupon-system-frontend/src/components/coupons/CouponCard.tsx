import React, { useState } from 'react';
import {
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Typography,
  Button,
  Box,
  Chip,
} from '@mui/material';
import { format } from 'date-fns';
import { Coupon } from '../../types/coupon.types';
import { getCategoryName } from '../../utils/categoryHelper';

interface CouponCardProps {
  coupon: Coupon;
  onPurchase?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
  showActions?: boolean;
}

export const CouponCard: React.FC<CouponCardProps> = ({
  coupon,
  onPurchase,
  onEdit,
  onDelete,
  showActions = true,
}) => {
  const [elevation, setElevation] = useState(1);

  const isOutOfStock = coupon.amount === 0;
  const categoryName = getCategoryName(coupon.CATEGORY);

  // Parse dates - handle both ISO string and Date formats
  const startDate = new Date(coupon.startDate);
  const endDate = new Date(coupon.endDate);

  const formattedStartDate = format(startDate, 'MMM dd, yyyy');
  const formattedEndDate = format(endDate, 'MMM dd, yyyy');

  const handleMouseEnter = () => {
    if (!isOutOfStock) {
      setElevation(8);
    }
  };

  const handleMouseLeave = () => {
    setElevation(1);
  };

  return (
    <Card
      data-testid="coupon-card"
      elevation={elevation}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        transition: 'all 0.3s ease-in-out',
        opacity: isOutOfStock ? 0.6 : 1,
        cursor: isOutOfStock ? 'not-allowed' : 'pointer',
      }}
    >
      {/* Image Section */}
      <CardMedia
        component="img"
        height={200}
        image={coupon.image}
        alt={coupon.title}
        sx={{
          backgroundColor: '#f0f0f0',
          objectFit: 'cover',
        }}
      />

      {/* Out of Stock Overlay */}
      {isOutOfStock && (
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            zIndex: 1,
          }}
        >
          <Typography
            variant="h6"
            sx={{
              color: 'white',
              fontWeight: 600,
            }}
          >
            Out of Stock
          </Typography>
        </Box>
      )}

      {/* Content Section */}
      <CardContent sx={{ flexGrow: 1, pb: 1 }}>
        {/* Title */}
        <Typography
          variant="h6"
          component="div"
          sx={{
            mb: 1,
            fontWeight: 600,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
          }}
        >
          {coupon.title}
        </Typography>

        {/* Category Chip */}
        <Box sx={{ mb: 1.5 }}>
          <Chip
            label={categoryName}
            size="small"
            variant="outlined"
            color="primary"
          />
        </Box>

        {/* Description */}
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            mb: 1.5,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            minHeight: '2.8em',
          }}
        >
          {coupon.description}
        </Typography>

        {/* Price and Stock Section */}
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mb: 1.5,
            p: 1,
            backgroundColor: '#f5f5f5',
            borderRadius: 1,
          }}
        >
          <Box>
            <Typography
              variant="caption"
              color="text.secondary"
              display="block"
            >
              Price
            </Typography>
            <Typography
              variant="h6"
              sx={{
                color: 'primary.main',
                fontWeight: 700,
              }}
            >
              ${coupon.price.toFixed(2)}
            </Typography>
          </Box>
          <Box sx={{ textAlign: 'right' }}>
            <Typography
              variant="caption"
              color="text.secondary"
              display="block"
            >
              Available
            </Typography>
            <Typography
              variant="h6"
              sx={{
                color: isOutOfStock ? 'error.main' : 'success.main',
                fontWeight: 700,
              }}
            >
              {coupon.amount}
            </Typography>
          </Box>
        </Box>

        {/* Dates Section */}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
          <Typography variant="caption" color="text.secondary">
            <strong>Start:</strong> {formattedStartDate}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            <strong>End:</strong> {formattedEndDate}
          </Typography>
        </Box>
      </CardContent>

      {/* Actions Section */}
      {showActions && (
        <CardActions
          sx={{
            display: 'flex',
            gap: 1,
            justifyContent: 'space-between',
            pt: 0,
            flexDirection: 'row',
          }}
        >
          {onPurchase && (
            <Button
              size="small"
              variant="contained"
              color="primary"
              onClick={onPurchase}
              disabled={isOutOfStock}
              fullWidth
            >
              Purchase
            </Button>
          )}

          {onEdit && (
            <Button
              size="small"
              variant="outlined"
              color="primary"
              onClick={onEdit}
              disabled={isOutOfStock}
              fullWidth={!onDelete}
            >
              Edit
            </Button>
          )}

          {onDelete && (
            <Button
              size="small"
              variant="outlined"
              color="error"
              onClick={onDelete}
              fullWidth={!onEdit}
            >
              Delete
            </Button>
          )}
        </CardActions>
      )}
    </Card>
  );
};
