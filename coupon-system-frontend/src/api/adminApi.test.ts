import { describe, it, expect } from 'vitest';
import { adminApi } from './adminApi';

describe('adminApi', () => {
  describe('Company Management', () => {
    describe('getAllCompanies', () => {
      it('should fetch all companies as array', async () => {
        const companies = await adminApi.getAllCompanies();
        expect(Array.isArray(companies)).toBe(true);
        expect(companies.length).toBeGreaterThan(0);
      });

      it('should return companies with correct structure', async () => {
        const companies = await adminApi.getAllCompanies();

        if (companies.length > 0) {
          const company = companies[0];
          expect(company).toHaveProperty('id');
          expect(company).toHaveProperty('name');
          expect(company).toHaveProperty('email');
        }
      });

      it('should return multiple companies', async () => {
        const companies = await adminApi.getAllCompanies();
        expect(companies.length).toBeGreaterThan(1);
      });
    });

    describe('getCompanyById', () => {
      it('should fetch single company by ID', async () => {
        const company = await adminApi.getCompanyById(1);

        expect(company).toBeDefined();
        expect(company.id).toBe(1);
      });

      it('should return company with all required fields', async () => {
        const company = await adminApi.getCompanyById(5);

        expect(company).toHaveProperty('id');
        expect(company).toHaveProperty('name');
        expect(company).toHaveProperty('email');
      });

      it('should throw error for non-existent company', async () => {
        await expect(adminApi.getCompanyById(999)).rejects.toThrow();
      });

      it('should handle different company IDs', async () => {
        const company1 = await adminApi.getCompanyById(1);
        const company2 = await adminApi.getCompanyById(2);

        expect(company1.id).toBe(1);
        expect(company2.id).toBe(2);
      });
    });

    describe('createCompany', () => {
      const validCompany = {
        name: 'New Test Company',
        email: 'newcompany@test.com',
        password: 'Password123',
      };

      it('should successfully create a company', async () => {
        const newCompany = await adminApi.createCompany(validCompany);

        expect(newCompany).toBeDefined();
        expect(newCompany).toHaveProperty('id');
        expect(newCompany.name).toBe(validCompany.name);
        expect(newCompany.email).toBe(validCompany.email);
      });

      it('should return created company with generated ID', async () => {
        const newCompany = await adminApi.createCompany(validCompany);

        expect(newCompany.id).toBeDefined();
        expect(typeof newCompany.id).toBe('number');
        expect(newCompany.id).toBeGreaterThan(0);
      });

      it('should preserve company name and email', async () => {
        const newCompany = await adminApi.createCompany(validCompany);

        expect(newCompany.name).toBe(validCompany.name);
        expect(newCompany.email).toBe(validCompany.email);
      });

      it('should handle different company names', async () => {
        const company1 = await adminApi.createCompany({
          ...validCompany,
          name: 'Company One',
        });
        const company2 = await adminApi.createCompany({
          ...validCompany,
          name: 'Company Two',
        });

        expect(company1.name).toBe('Company One');
        expect(company2.name).toBe('Company Two');
      });
    });

    describe('updateCompany', () => {
      const updateData = {
        name: 'Updated Company Name',
        email: 'updated@test.com',
      };

      it('should successfully update a company', async () => {
        const updated = await adminApi.updateCompany(1, updateData);

        expect(updated).toBeDefined();
        expect(updated.id).toBe(1);
      });

      it('should preserve company ID after update', async () => {
        const companyId = 5;
        const updated = await adminApi.updateCompany(companyId, updateData);

        expect(updated.id).toBe(companyId);
      });

      it('should update all provided fields', async () => {
        const updated = await adminApi.updateCompany(1, {
          name: 'New Name',
        });

        expect(updated.name).toBe('New Name');
      });

      it('should handle multiple sequential updates', async () => {
        const update1 = await adminApi.updateCompany(1, { name: 'First Update' });
        const update2 = await adminApi.updateCompany(1, { name: 'Second Update' });

        expect(update1.name).toBe('First Update');
        expect(update2.name).toBe('Second Update');
      });
    });

    describe('deleteCompany', () => {
      it('should successfully delete a company', async () => {
        await expect(adminApi.deleteCompany(1)).resolves.toBeUndefined();
      });

      it('should handle deleting different companies', async () => {
        await expect(adminApi.deleteCompany(5)).resolves.toBeUndefined();
        await expect(adminApi.deleteCompany(10)).resolves.toBeUndefined();
      });

      it('should throw error for non-existent company', async () => {
        await expect(adminApi.deleteCompany(999)).rejects.toThrow();
      });

      it('should handle multiple sequential deletes', async () => {
        await adminApi.deleteCompany(1);
        await adminApi.deleteCompany(2);
        await adminApi.deleteCompany(3);
        // All should succeed
      });
    });
  });

  describe('Customer Management', () => {
    describe('getAllCustomers', () => {
      it('should fetch all customers as array', async () => {
        const customers = await adminApi.getAllCustomers();
        expect(Array.isArray(customers)).toBe(true);
        expect(customers.length).toBeGreaterThan(0);
      });

      it('should return customers with correct structure', async () => {
        const customers = await adminApi.getAllCustomers();

        if (customers.length > 0) {
          const customer = customers[0];
          expect(customer).toHaveProperty('id');
          expect(customer).toHaveProperty('firstName');
          expect(customer).toHaveProperty('lastName');
          expect(customer).toHaveProperty('email');
        }
      });

      it('should return multiple customers', async () => {
        const customers = await adminApi.getAllCustomers();
        expect(customers.length).toBeGreaterThan(1);
      });
    });

    describe('getCustomerById', () => {
      it('should fetch single customer by ID', async () => {
        const customer = await adminApi.getCustomerById(1);

        expect(customer).toBeDefined();
        expect(customer.id).toBe(1);
      });

      it('should return customer with all required fields', async () => {
        const customer = await adminApi.getCustomerById(5);

        expect(customer).toHaveProperty('id');
        expect(customer).toHaveProperty('firstName');
        expect(customer).toHaveProperty('lastName');
        expect(customer).toHaveProperty('email');
      });

      it('should throw error for non-existent customer', async () => {
        await expect(adminApi.getCustomerById(999)).rejects.toThrow();
      });

      it('should handle different customer IDs', async () => {
        const customer1 = await adminApi.getCustomerById(1);
        const customer2 = await adminApi.getCustomerById(2);

        expect(customer1.id).toBe(1);
        expect(customer2.id).toBe(2);
      });
    });

    describe('createCustomer', () => {
      const validCustomer = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@test.com',
        password: 'Password123',
      };

      it('should successfully create a customer', async () => {
        const newCustomer = await adminApi.createCustomer(validCustomer);

        expect(newCustomer).toBeDefined();
        expect(newCustomer).toHaveProperty('id');
        expect(newCustomer.firstName).toBe(validCustomer.firstName);
        expect(newCustomer.lastName).toBe(validCustomer.lastName);
        expect(newCustomer.email).toBe(validCustomer.email);
      });

      it('should return created customer with generated ID', async () => {
        const newCustomer = await adminApi.createCustomer(validCustomer);

        expect(newCustomer.id).toBeDefined();
        expect(typeof newCustomer.id).toBe('number');
        expect(newCustomer.id).toBeGreaterThan(0);
      });

      it('should preserve customer name and email', async () => {
        const newCustomer = await adminApi.createCustomer(validCustomer);

        expect(newCustomer.firstName).toBe(validCustomer.firstName);
        expect(newCustomer.lastName).toBe(validCustomer.lastName);
        expect(newCustomer.email).toBe(validCustomer.email);
      });

      it('should handle different customer names', async () => {
        const customer1 = await adminApi.createCustomer({
          ...validCustomer,
          firstName: 'Alice',
          lastName: 'Smith',
        });
        const customer2 = await adminApi.createCustomer({
          ...validCustomer,
          firstName: 'Bob',
          lastName: 'Jones',
        });

        expect(customer1.firstName).toBe('Alice');
        expect(customer2.firstName).toBe('Bob');
      });
    });

    describe('updateCustomer', () => {
      const updateData = {
        firstName: 'Updated',
        lastName: 'Name',
        email: 'updated@test.com',
      };

      it('should successfully update a customer', async () => {
        const updated = await adminApi.updateCustomer(1, updateData);

        expect(updated).toBeDefined();
        expect(updated.id).toBe(1);
      });

      it('should preserve customer ID after update', async () => {
        const customerId = 5;
        const updated = await adminApi.updateCustomer(customerId, updateData);

        expect(updated.id).toBe(customerId);
      });

      it('should update all provided fields', async () => {
        const updated = await adminApi.updateCustomer(1, {
          firstName: 'NewFirstName',
        });

        expect(updated.firstName).toBe('NewFirstName');
      });

      it('should handle multiple sequential updates', async () => {
        const update1 = await adminApi.updateCustomer(1, { firstName: 'First' });
        const update2 = await adminApi.updateCustomer(1, { firstName: 'Second' });

        expect(update1.firstName).toBe('First');
        expect(update2.firstName).toBe('Second');
      });
    });

    describe('deleteCustomer', () => {
      it('should successfully delete a customer', async () => {
        await expect(adminApi.deleteCustomer(1)).resolves.toBeUndefined();
      });

      it('should handle deleting different customers', async () => {
        await expect(adminApi.deleteCustomer(5)).resolves.toBeUndefined();
        await expect(adminApi.deleteCustomer(10)).resolves.toBeUndefined();
      });

      it('should throw error for non-existent customer', async () => {
        await expect(adminApi.deleteCustomer(999)).rejects.toThrow();
      });

      it('should handle multiple sequential deletes', async () => {
        await adminApi.deleteCustomer(1);
        await adminApi.deleteCustomer(2);
        await adminApi.deleteCustomer(3);
        // All should succeed
      });
    });
  });

  describe('Other Admin Operations', () => {
    describe('unlockAccount', () => {
      it('should successfully unlock an account', async () => {
        await expect(adminApi.unlockAccount('user@test.com')).resolves.toBeUndefined();
      });

      it('should handle unlocking different accounts', async () => {
        await expect(adminApi.unlockAccount('user1@test.com')).resolves.toBeUndefined();
        await expect(adminApi.unlockAccount('user2@test.com')).resolves.toBeUndefined();
      });

      it('should handle company email addresses', async () => {
        await expect(adminApi.unlockAccount('company@test.com')).resolves.toBeUndefined();
      });

      it('should handle customer email addresses', async () => {
        await expect(adminApi.unlockAccount('customer@test.com')).resolves.toBeUndefined();
      });

      it('should handle multiple sequential unlock calls', async () => {
        await adminApi.unlockAccount('user1@test.com');
        await adminApi.unlockAccount('user2@test.com');
        await adminApi.unlockAccount('user3@test.com');
        // All should succeed
      });
    });

    describe('getAllCoupons', () => {
      it('should fetch all coupons as array', async () => {
        const coupons = await adminApi.getAllCoupons();
        expect(Array.isArray(coupons)).toBe(true);
        expect(coupons.length).toBeGreaterThan(0);
      });

      it('should return coupons with correct structure', async () => {
        const coupons = await adminApi.getAllCoupons();

        if (coupons.length > 0) {
          const coupon = coupons[0];
          expect(coupon).toHaveProperty('id');
          expect(coupon).toHaveProperty('companyID');
          expect(coupon).toHaveProperty('CATEGORY');
          expect(coupon).toHaveProperty('title');
          expect(coupon).toHaveProperty('description');
          expect(coupon).toHaveProperty('price');
          expect(coupon).toHaveProperty('amount');
        }
      });

      it('should return multiple coupons from different companies', async () => {
        const coupons = await adminApi.getAllCoupons();
        expect(coupons.length).toBeGreaterThan(5);
      });
    });
  });

  describe('integration scenarios', () => {
    it('should support full company management workflow', async () => {
      // 1. Get all companies
      const companies = await adminApi.getAllCompanies();
      expect(companies.length).toBeGreaterThan(0);

      // 2. Create a new company
      const newCompany = await adminApi.createCompany({
        name: 'Test Company',
        email: 'test@company.com',
        password: 'Password123',
      });
      expect(newCompany.id).toBeDefined();

      // 3. Update the company
      const updated = await adminApi.updateCompany(newCompany.id, {
        name: 'Updated Test Company',
      });
      expect(updated.name).toBe('Updated Test Company');

      // 4. Get company by ID
      const fetched = await adminApi.getCompanyById(newCompany.id);
      expect(fetched.id).toBe(newCompany.id);

      // 5. Delete the company
      await expect(adminApi.deleteCompany(newCompany.id)).resolves.toBeUndefined();
    });

    it('should support full customer management workflow', async () => {
      // 1. Get all customers
      const customers = await adminApi.getAllCustomers();
      expect(customers.length).toBeGreaterThan(0);

      // 2. Create a new customer
      const newCustomer = await adminApi.createCustomer({
        firstName: 'Test',
        lastName: 'User',
        email: 'testuser@test.com',
        password: 'Password123',
      });
      expect(newCustomer.id).toBeDefined();

      // 3. Update the customer
      const updated = await adminApi.updateCustomer(newCustomer.id, {
        firstName: 'Updated',
      });
      expect(updated.firstName).toBe('Updated');

      // 4. Get customer by ID
      const fetched = await adminApi.getCustomerById(newCustomer.id);
      expect(fetched.id).toBe(newCustomer.id);

      // 5. Delete the customer
      await expect(adminApi.deleteCustomer(newCustomer.id)).resolves.toBeUndefined();
    });

    it('should support account unlock workflow', async () => {
      // 1. Get all customers
      const customers = await adminApi.getAllCustomers();
      expect(customers.length).toBeGreaterThan(0);

      // 2. Unlock a customer account
      const customer = customers[0];
      await expect(adminApi.unlockAccount(customer.email)).resolves.toBeUndefined();

      // 3. Get all companies
      const companies = await adminApi.getAllCompanies();
      expect(companies.length).toBeGreaterThan(0);

      // 4. Unlock a company account
      const company = companies[0];
      await expect(adminApi.unlockAccount(company.email)).resolves.toBeUndefined();
    });
  });
});
