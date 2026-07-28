# Ticket 003: Migrate the primary branch to `main`

- Status: Needs info
- Specification: Not required
- Blocking tickets: None

## Scope

Create `main` from the complete `master` history, update repository references
that assume `master`, and make `main` the GitHub default branch. Keep `master`
until the new default branch and all critical references have been verified.

## Acceptance criteria

- `main` contains every commit reachable from `master` at the migration point.
- GitHub's default branch is `main`, and a fresh clone checks out `main`.
- Push CI runs on `main`, while pull request CI continues to cover proposed
  changes.
- Repository documentation does not link to assets through `master`.
- `master` is not deleted until the new default branch and critical references
  are verified.

## Verification

2026-07-28:

- Live `git ls-remote --symref origin HEAD refs/heads/master refs/heads/main`
  reported `HEAD` at `refs/heads/master`, with `master` at
  `92d6fad377117e9255459ad1e4583917227c1ec3` and no remote `main`.
- Local `main` was created from the same commit, preserving all reachable
  history.
- Remote `main` was published at
  `05b2504956fc94a9406519f7011d4beb4273fad9`; `origin/master` remains unchanged
  at the migration point and is an ancestor of `origin/main`.
- `.github/workflows/pre-merge.yaml` now runs push checks for `main`.
- `README.md` now loads the sample asset from `main`.
- GitHub Actions run `30341119949` completed successfully for the `main` push at
  `05b2504956fc94a9406519f7011d4beb4273fad9`.
- A fresh clone still checked out `master`, confirming that GitHub's hosted
  default branch has not changed.
- GitHub CLI authentication for `soarcn` is invalid, so changing and verifying
  the hosted default branch requires re-authentication or a maintainer action.
- `master` remains intact locally and remotely while the default-branch change
  is blocked.

## Remaining work

- Re-authenticate GitHub CLI with repository administration permission.
- Change the GitHub default branch to `main`.
- Verify the remote symbolic `HEAD` and a fresh clone's checked-out branch.
- Re-scan critical references before deciding separately whether to delete
  `master`.
