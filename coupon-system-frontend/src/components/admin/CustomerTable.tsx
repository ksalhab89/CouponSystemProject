import React, { useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Paper,
  IconButton,
  Box,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  Tooltip,
  TableSortLabel,
} from '@mui/material';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  Lock as LockIcon,
} from '@mui/icons-material';
import { Customer } from '../../types/coupon.types';
import { LoadingSpinner } from '../common/LoadingSpinner';

type SortableColumn = 'id' | 'firstName' | 'lastName' | 'email';
type SortOrder = 'asc' | 'desc';

interface CustomerTableProps {
  customers: Customer[];
  onEdit: (customer: Customer) => void;
  onDelete: (customerId: number) => Promise<void>;
  onUnlock?: (customerId: number) => Promise<void>;
  loading?: boolean;
}

export const CustomerTable: React.FC<CustomerTableProps> = ({
  customers,
  onEdit,
  onDelete,
  onUnlock,
  loading = false,
}) => {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [orderBy, setOrderBy] = useState<SortableColumn>('id');
  const [order, setOrder] = useState<SortOrder>('asc');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [unlockDialogOpen, setUnlockDialogOpen] = useState(false);
  const [selectedCustomerId, setSelectedCustomerId] = useState<number | null>(
    null
  );
  const [isProcessing, setIsProcessing] = useState(false);

  // Handle sort
  const handleSort = (column: SortableColumn) => {
    if (orderBy === column) {
      setOrder(order === 'asc' ? 'desc' : 'asc');
    } else {
      setOrderBy(column);
      setOrder('asc');
    }
    setPage(0);
  };

  // Sort customers
  const sortedCustomers = [...customers].sort((a, b) => {
    const aValue = a[orderBy];
    const bValue = b[orderBy];

    if (typeof aValue === 'string' && typeof bValue === 'string') {
      return order === 'asc'
        ? aValue.localeCompare(bValue)
        : bValue.localeCompare(aValue);
    }

    if (typeof aValue === 'number' && typeof bValue === 'number') {
      return order === 'asc' ? aValue - bValue : bValue - aValue;
    }

    return 0;
  });

  // Paginate
  const paginatedCustomers = sortedCustomers.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  // Delete dialog handlers
  const openDeleteDialog = (customerId: number) => {
    setSelectedCustomerId(customerId);
    setDeleteDialogOpen(true);
  };

  const closeDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setSelectedCustomerId(null);
  };

  const handleConfirmDelete = async () => {
    if (selectedCustomerId === null) return;

    setIsProcessing(true);
    try {
      await onDelete(selectedCustomerId);
    } finally {
      setIsProcessing(false);
      closeDeleteDialog();
    }
  };

  // Unlock dialog handlers
  const openUnlockDialog = (customerId: number) => {
    setSelectedCustomerId(customerId);
    setUnlockDialogOpen(true);
  };

  const closeUnlockDialog = () => {
    setUnlockDialogOpen(false);
    setSelectedCustomerId(null);
  };

  const handleConfirmUnlock = async () => {
    if (selectedCustomerId === null || !onUnlock) return;

    setIsProcessing(true);
    try {
      await onUnlock(selectedCustomerId);
    } finally {
      setIsProcessing(false);
      closeUnlockDialog();
    }
  };

  if (loading) {
    return (
      <Paper sx={{ p: 3, minHeight: '500px' }}>
        <LoadingSpinner message="Loading customers..." />
      </Paper>
    );
  }

  if (customers.length === 0) {
    return (
      <Paper sx={{ p: 3 }}>
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '300px',
            gap: 2,
          }}
        >
          <Typography variant="h6" color="textSecondary">
            No customers found
          </Typography>
          <Typography variant="body2" color="textSecondary">
            There are no customers in the system yet.
          </Typography>
        </Box>
      </Paper>
    );
  }

  return (
    <>
      <TableContainer component={Paper}>
        <Table sx={{ minWidth: 650 }} aria-label="customers table">
          <TableHead>
            <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'id'}
                  direction={orderBy === 'id' ? order : 'asc'}
                  onClick={() => handleSort('id')}
                >
                  ID
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'firstName'}
                  direction={orderBy === 'firstName' ? order : 'asc'}
                  onClick={() => handleSort('firstName')}
                >
                  First Name
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'lastName'}
                  direction={orderBy === 'lastName' ? order : 'asc'}
                  onClick={() => handleSort('lastName')}
                >
                  Last Name
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'email'}
                  direction={orderBy === 'email' ? order : 'asc'}
                  onClick={() => handleSort('email')}
                >
                  Email
                </TableSortLabel>
              </TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paginatedCustomers.map((customer) => (
              <TableRow
                key={customer.id}
                sx={{ '&:hover': { backgroundColor: '#fafafa' } }}
              >
                <TableCell>{customer.id}</TableCell>
                <TableCell>{customer.firstName}</TableCell>
                <TableCell>{customer.lastName}</TableCell>
                <TableCell>{customer.email}</TableCell>
                <TableCell align="right">
                  <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                    <Tooltip title="Edit">
                      <IconButton
                        size="small"
                        color="primary"
                        onClick={() => onEdit(customer)}
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    {onUnlock && (
                      <Tooltip title="Unlock">
                        <IconButton
                          size="small"
                          color="warning"
                          onClick={() => openUnlockDialog(customer.id)}
                        >
                          <LockIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}
                    <Tooltip title="Delete">
                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => openDeleteDialog(customer.id)}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <TablePagination
        rowsPerPageOptions={[5, 10, 25, 50]}
        component="div"
        count={sortedCustomers.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={closeDeleteDialog}
        aria-labelledby="delete-dialog-title"
      >
        <DialogTitle id="delete-dialog-title">Confirm Delete</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete this customer? This action cannot be
            undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={closeDeleteDialog}
            disabled={isProcessing}
            color="inherit"
          >
            Cancel
          </Button>
          <Button
            onClick={handleConfirmDelete}
            disabled={isProcessing}
            color="error"
            variant="contained"
          >
            {isProcessing ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Unlock Confirmation Dialog */}
      <Dialog
        open={unlockDialogOpen}
        onClose={closeUnlockDialog}
        aria-labelledby="unlock-dialog-title"
      >
        <DialogTitle id="unlock-dialog-title">Confirm Unlock</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to unlock this customer account? The customer
            will be able to log in again.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={closeUnlockDialog}
            disabled={isProcessing}
            color="inherit"
          >
            Cancel
          </Button>
          <Button
            onClick={handleConfirmUnlock}
            disabled={isProcessing}
            color="warning"
            variant="contained"
          >
            {isProcessing ? 'Unlocking...' : 'Unlock'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};
