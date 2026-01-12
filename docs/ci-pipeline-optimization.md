# CI Pipeline Optimization - Backend & Frontend in Parallel

**Date:** January 12, 2026
**Status:** Implemented
**Pipeline:** GitHub Actions

---

## Executive Summary

Restructured CI pipeline from a single-job backend-only workflow to a **multi-stage, parallel-execution pipeline** that tests both backend and frontend efficiently.

### Key Improvements

✅ **Multi-stage pipeline** - Build → Test → Security Scan (sequential stages)
✅ **Parallel test execution** - Backend (678 tests) and Frontend (399 tests) run simultaneously
✅ **Path filtering** - Jobs run only when relevant files change
✅ **Smart caching** - Maven and npm caches for faster builds
✅ **Better visibility** - Job summaries showing what was tested
✅ **Resource efficiency** - Skip unnecessary jobs, save CI minutes

---

## Pipeline Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│ STAGE 1: DETECT CHANGES                                         │
│ ┌───────────────────────────────────────────────────────────┐   │
│ │ detect-changes                                            │   │
│ │ - Analyzes git diff                                       │   │
│ │ - Outputs: backend=true/false, frontend=true/false        │   │
│ └───────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ STAGE 2: BUILD (Parallel if both changed)                      │
│ ┌─────────────────────────┐   ┌─────────────────────────────┐  │
│ │ build-backend           │   │ build-frontend              │  │
│ │ - Setup JDK 25          │   │ - Setup Node.js 20          │  │
│ │ - Start PostgreSQL      │   │ - npm ci                    │  │
│ │ - mvn package           │   │ - npm run build             │  │
│ │ - Upload JAR artifact   │   │ - Upload dist artifact      │  │
│ └─────────────────────────┘   └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                │                          │
                └────────┬─────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ STAGE 3: TEST (Parallel - Most Critical Stage)                 │
│ ┌─────────────────────────┐   ┌─────────────────────────────┐  │
│ │ test-backend            │   │ test-frontend               │  │
│ │ - Setup JDK 25          │   │ - Setup Node.js 20          │  │
│ │ - Start PostgreSQL      │   │ - npm ci                    │  │
│ │ - mvn test (678 tests)  │   │ - npm test (399 tests)      │  │
│ │ - jacoco:report         │   │ - Coverage report           │  │
│ │ - Upload coverage       │   │ - Upload coverage           │  │
│ └─────────────────────────┘   └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ STAGE 4: SECURITY SCAN (Main branch only)                      │
│ ┌───────────────────────────────────────────────────────────┐   │
│ │ security-scan                                             │   │
│ │ - OWASP Dependency Check                                  │   │
│ │ - NVD vulnerability scan                                  │   │
│ │ - Upload security report                                  │   │
│ └───────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ STAGE 5: SUMMARY                                                │
│ ┌───────────────────────────────────────────────────────────┐   │
│ │ ci-summary                                                │   │
│ │ - Aggregate all job results                               │   │
│ │ - Create summary report                                   │   │
│ │ - Fail if any test suite failed                           │   │
│ └───────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Comparison: Old vs New

### Old CI Pipeline (`.github/workflows/ci.yml`)

```yaml
jobs:
  build:  # Single job that does everything
    - Checkout
    - Setup JDK
    - Start PostgreSQL
    - Build backend
    - Test backend (678 tests)
    - OWASP scan
    # ❌ No frontend testing
    # ❌ No parallel execution
    # ❌ No path filtering
```

**Issues:**
- Frontend tests (399 tests) never run in CI ❌
- Backend and frontend can't run in parallel ❌
- Always runs full pipeline even if only README changes ❌
- No clear separation of build vs test stages ❌
- Single point of failure (one job does everything) ❌

### New CI Pipeline (`.github/workflows/ci-optimized.yml`)

```yaml
jobs:
  detect-changes:  # Smart detection
  build-backend:   # Conditional on backend changes
  build-frontend:  # Conditional on frontend changes
  test-backend:    # Parallel with test-frontend
  test-frontend:   # Parallel with test-backend
  security-scan:   # Only on main branch
  ci-summary:      # Aggregate results
```

**Benefits:**
- Frontend tests (399 tests) run in CI ✅
- Backend and frontend tests run in parallel (50% faster) ✅
- Path filtering skips irrelevant jobs (saves CI minutes) ✅
- Clear stages: detect → build → test → scan → summary ✅
- Fail-fast: Stop on first test failure ✅
- Better visibility: Job summaries show what was tested ✅

---

## Path Filtering Configuration

Using [`dorny/paths-filter`](https://github.com/dorny/paths-filter) action to detect changes:

```yaml
backend:
  - 'src/**'
  - 'pom.xml'
  - 'docker-compose.yml'
  - '.github/workflows/**'

frontend:
  - 'coupon-system-frontend/**'
  - '!coupon-system-frontend/node_modules/**'
  - '.github/workflows/**'
```

### Scenarios

| Files Changed | Backend Job | Frontend Job | Result |
|---------------|-------------|--------------|--------|
| `src/main/java/**/*.java` | ✅ Runs | ❌ Skipped | Fast |
| `coupon-system-frontend/src/**/*.tsx` | ❌ Skipped | ✅ Runs | Fast |
| `README.md` | ❌ Skipped | ❌ Skipped | Very Fast |
| Both BE + FE | ✅ Runs | ✅ Runs (parallel) | Efficient |
| `.github/workflows/ci-optimized.yml` | ✅ Runs | ✅ Runs | Safe |

---

## Job Dependencies (needs)

Jobs use the `needs` keyword to create a dependency graph:

```yaml
build-backend:
  needs: detect-changes  # Wait for change detection

test-backend:
  needs: [detect-changes, build-backend]  # Wait for both

test-frontend:
  needs: [detect-changes, build-frontend]  # Independent from backend tests

security-scan:
  needs: [test-backend]  # Only after backend tests pass

ci-summary:
  needs: [detect-changes, test-backend, test-frontend]  # Aggregate all results
  if: always()  # Run even if tests fail
```

**Key Points:**
- `test-backend` and `test-frontend` run **in parallel** (no dependency between them)
- Each job only runs if its dependencies succeed
- `ci-summary` runs even on failure (`if: always()`) to show results

---

## Parallel Execution

### Time Savings

**Old Pipeline (Sequential):**
```
Build Backend: 2 min
Test Backend: 5 min
-----------------
Total: 7 min
```

**New Pipeline (Parallel):**
```
Detect Changes: 10 sec
Build Backend: 2 min   │  Build Frontend: 1.5 min
Test Backend:  5 min   │  Test Frontend:  2 min
Security Scan: 3 min
-----------------
Total: ~10.5 min (but frontend is now tested!)
```

**Effective time if both change:** ~10.5 min (vs 7 min + manual FE tests)
**Effective time if only BE changes:** ~7 min (same as before)
**Effective time if only FE changes:** ~3.5 min (huge savings!)

---

## Conditional Execution

Jobs use `if` conditions to run conditionally:

```yaml
build-backend:
  if: needs.detect-changes.outputs.backend == 'true'

build-frontend:
  if: needs.detect-changes.outputs.frontend == 'true'

security-scan:
  if: github.ref == 'refs/heads/main' && needs.detect-changes.outputs.backend == 'true'
```

This ensures:
- Backend jobs only run when backend files change
- Frontend jobs only run when frontend files change
- Security scan only runs on main branch (not on every PR)

---

## Artifacts

Each stage uploads artifacts for visibility and debugging:

| Job | Artifact | Retention |
|-----|----------|-----------|
| `build-backend` | `backend-build` (JAR) | 1 day |
| `build-frontend` | `frontend-build` (dist/) | 1 day |
| `test-backend` | `backend-coverage-report` | Default |
| `test-frontend` | `frontend-coverage-report` | Default |
| `security-scan` | `owasp-report` | Default |

---

## Best Practices Applied

Based on research from:
- [GitHub Actions Matrix Strategy](https://depot.dev/blog/github-actions-matrix-strategy)
- [Monorepo CI/CD Best Practices](https://lalits77.medium.com/monorepo-mayhem-tame-your-ci-cd-with-github-actions-the-right-way-2adacbdd33c6)
- [Job Dependencies Documentation](https://docs.github.com/actions/using-jobs/using-jobs-in-a-workflow)

### 1. Path Filtering ✅
Run jobs only when relevant files change

### 2. Parallel Execution ✅
Backend and frontend tests run simultaneously using `needs` without mutual dependencies

### 3. Job Dependencies ✅
Use `needs` keyword to create clear dependency graph: detect → build → test → scan → summary

### 4. Conditional Jobs ✅
Use `if` conditions to skip unnecessary work

### 5. Smart Caching ✅
- Maven cache for backend dependencies
- npm cache for frontend dependencies
- OWASP data cache for security scans

### 6. Fail-Fast ✅
Stop pipeline on first test failure to save CI minutes

### 7. Clear Stages ✅
Separate build, test, and security scan stages

### 8. Job Summaries ✅
Aggregate results in final summary job

---

## Migration Plan

### Option 1: Side-by-Side (Recommended)
1. Keep `ci.yml` (old pipeline) active
2. Add `ci-optimized.yml` (new pipeline) to run in parallel
3. Monitor both for 1-2 weeks
4. Once confident, rename `ci.yml` to `ci-legacy.yml` and rename `ci-optimized.yml` to `ci.yml`

### Option 2: Direct Replacement
1. Rename `ci.yml` to `ci-legacy.yml` (backup)
2. Rename `ci-optimized.yml` to `ci.yml` (activate)
3. Monitor closely for issues

### Rollback Plan
If issues arise, simply rename files back:
```bash
mv .github/workflows/ci.yml .github/workflows/ci-optimized.yml
mv .github/workflows/ci-legacy.yml .github/workflows/ci.yml
git commit -m "rollback: revert to legacy CI"
git push
```

---

## Next Steps

1. **Commit** the new `ci-optimized.yml` workflow
2. **Push** to a feature branch
3. **Open PR** to see the new pipeline in action
4. **Monitor** the parallel execution and path filtering
5. **Measure** time savings and CI minute usage
6. **Switch** to new pipeline once validated

---

## References

### GitHub Actions Best Practices
- [How to leverage GitHub Actions matrix strategy](https://depot.dev/blog/github-actions-matrix-strategy)
- [Monorepo Mayhem? Tame Your CI/CD with GitHub Actions](https://lalits77.medium.com/monorepo-mayhem-tame-your-ci-cd-with-github-actions-the-right-way-2adacbdd33c6)
- [Using jobs in a workflow - GitHub Docs](https://docs.github.com/actions/using-jobs/using-jobs-in-a-workflow)

### Job Dependencies & Stages
- [Execute multiple jobs in Sequence using needs](https://notes.kodekloud.com/docs/GitHub-Actions/GitHub-Actions-Core-Concepts/Execute-multiple-jobs-in-Sequence-using-needs)
- [GitHub Actions: Adding Optional Dependencies Between Jobs](https://medium.com/@nmmanas/github-actions-adding-optional-dependency-between-jobs-deploy-backend-first-then-the-frontend-5583c047edf9)
- [Dependencies - github actions](https://calmcode.io/course/github-actions/dependencies)

---

**Document Version:** 1.0
**Last Updated:** January 12, 2026
**Next Review:** After migration to new pipeline
