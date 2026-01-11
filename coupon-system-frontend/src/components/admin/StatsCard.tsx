import React from 'react';
import {
  Card,
  CardContent,
  Box,
  Typography,
  useTheme,
  useMediaQuery,
  SvgIconTypeMap,
} from '@mui/material';
import { OverridableComponent } from '@mui/material/OverridableComponent';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';

interface StatsCardProps {
  /**
   * The title/label for the statistic
   */
  title: string;

  /**
   * The main value to display (number or string)
   */
  count: number | string;

  /**
   * Icon component to display in the card
   */
  icon: OverridableComponent<SvgIconTypeMap<{}, 'svg'>> & { muiName: string };

  /**
   * Color theme for the card (hex color string)
   */
  color: string;

  /**
   * Background color (hex color string)
   */
  backgroundColor: string;

  /**
   * Optional trend indicator showing up/down movement
   */
  trend?: {
    direction: 'up' | 'down';
    percentage: number;
  };
}

export const StatsCard: React.FC<StatsCardProps> = ({
  title,
  count,
  icon: Icon,
  color,
  backgroundColor,
  trend,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.down('md'));

  return (
    <Card
      sx={{
        backgroundColor: backgroundColor,
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        padding: isMobile ? '16px' : isTablet ? '20px' : '24px',
        borderRadius: 2,
        transition: 'transform 0.3s ease, box-shadow 0.3s ease',
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: `0 8px 24px ${color}30`,
        },
      }}
    >
      <CardContent
        sx={{
          padding: 0,
          display: 'flex',
          flexDirection: 'column',
          height: '100%',
          '&:last-child': {
            paddingBottom: 0,
          },
        }}
      >
        {/* Header with Icon and Title */}
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'flex-start',
            marginBottom: isMobile ? '12px' : '16px',
          }}
        >
          <Typography
            variant={isMobile ? 'subtitle2' : 'subtitle1'}
            sx={{
              color: 'text.secondary',
              fontWeight: 500,
              flex: 1,
              marginRight: '8px',
              lineHeight: 1.4,
            }}
          >
            {title}
          </Typography>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: isMobile ? '32px' : '40px',
              height: isMobile ? '32px' : '40px',
              borderRadius: '8px',
              backgroundColor: `${color}20`,
              color: color,
              flexShrink: 0,
              fontSize: isMobile ? '20px' : '24px',
            }}
          >
            <Icon />
          </Box>
        </Box>

        {/* Main Value */}
        <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1, marginBottom: '8px' }}>
          <Typography
            variant={isMobile ? 'h5' : 'h4'}
            sx={{
              fontWeight: 700,
              color: 'text.primary',
              lineHeight: 1.2,
              wordBreak: 'break-word',
            }}
          >
            {typeof count === 'number' ? count.toLocaleString() : count}
          </Typography>
        </Box>

        {/* Trend Indicator */}
        {trend && (
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 0.5,
              marginTop: 'auto',
            }}
          >
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                color: trend.direction === 'up' ? theme.palette.success.main : theme.palette.error.main,
              }}
            >
              {trend.direction === 'up' ? (
                <TrendingUpIcon sx={{ fontSize: isMobile ? '16px' : '18px' }} />
              ) : (
                <TrendingDownIcon sx={{ fontSize: isMobile ? '16px' : '18px' }} />
              )}
            </Box>
            <Typography
              variant="caption"
              sx={{
                color: trend.direction === 'up' ? theme.palette.success.main : theme.palette.error.main,
                fontWeight: 600,
                fontSize: isMobile ? '0.7rem' : '0.75rem',
              }}
            >
              {Math.abs(trend.percentage)}% {trend.direction === 'up' ? 'increase' : 'decrease'}
            </Typography>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};
