import { test as teardown } from '@playwright/test';
import fs from 'fs';

/**
 * Optional cleanup to remove authentication state files after tests complete
 * This ensures fresh authentication on next test run
 */

teardown('remove auth files', async ({}) => {
  const authDir = './playwright/.auth';

  if (fs.existsSync(authDir)) {
    const files = fs.readdirSync(authDir);
    for (const file of files) {
      fs.unlinkSync(`${authDir}/${file}`);
      console.log(`🗑️  Removed ${file}`);
    }
    console.log('✅ Cleanup complete');
  }
});
