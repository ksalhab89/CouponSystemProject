# Coverage Gaps Analysis - Current: 93% (374 instructions missed)

## Goal
- **Phase 1**: Reach 95% overall (need to cover ~124 more instructions)
- **Phase 2**: Get auth/security/filters to 100%

## Current Status (by priority)

### Critical User-Facing Code (Target: 100%)

#### 1. backend.login - 70% (141 missed) ⚠️ PRIORITY #1
- LoginManager: 66% coverage
- Missing: lockout expiry edge cases, admin lockout disabled paths

#### 2. api.filter - 80% (87 missed) ⚠️ PRIORITY #2
- Need to analyze which filters need tests
- Focus: RequestResponseLoggingFilter, CORS edge cases

#### 3. security - 99% (5 missed) ✅ Nearly complete!
- JwtTokenProvider: 97% (5 missed) - minor cleanup needed
- All other security classes: 100%

### Secondary Gaps (For reaching 95%)

#### 4. backend.logging - 86% (32 missed)
- MetricsLogger or LoggingService gaps

#### 5. backend.facade - 95% (29 missed)
- Near complete, minor edge cases

#### 6. backend.periodicJob - 75% (17 missed)
- Scheduled task execution paths

#### 7. service - 95% (14 missed)
- AuthenticationService: 92% (minor gaps)

#### 8. backend.couponCategory - 84% (11 missed)
- Category enum/utility gaps

## Implementation Plan

### Phase 1: Reach 95% Overall (~124 instructions to cover)

**Step 1**: Complete LoginManager tests (141 missed → target 20 missed)
- Add auto-unlock expiry tests
- Add admin lockout disabled scenario tests
- Add edge cases for concurrent lockouts
- **Impact**: +121 instructions = 95% progress!

**Step 2**: If needed, add targeted filter tests (87 missed → target 50 missed)
- RequestResponseLoggingFilter edge cases
- CORS filter completions
- **Impact**: +37 instructions

### Phase 2: Get Critical Code to 100%

**Target packages**: backend.login, api.filter, security

**Step 1**: Finish LoginManager (20 → 0)
**Step 2**: Finish api.filter (50 → 0)
**Step 3**: Finish security JwtTokenProvider (5 → 0)

---

**Next Action**: Focus on LoginManager to add those missing ~120 instructions and push us over 95%!
