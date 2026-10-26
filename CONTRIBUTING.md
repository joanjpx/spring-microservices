# Contributing and Git Flow

The shared Git Flow branches are:

- `master`: production-ready history. Changes arrive through release or hotfix pull requests.
- `develop`: integration branch and starting point for normal work.

Use these branch prefixes:

- `feature/<short-description>`: new work; branch from `develop`, merge back into `develop`.
- `release/<version>`: release stabilization; branch from `develop`, merge into both `master` and `develop`.
- `hotfix/<short-description>`: urgent production fix; branch from `master`, merge into both `master` and `develop`.

Example for a feature:

```powershell
git switch develop
git pull --ff-only
git switch --create feature/my-change
# make and commit the change
git push --set-upstream origin feature/my-change
```

Open a pull request from `feature/my-change` into `develop`. Release and hotfix pull requests use the merge targets described above. Do not commit feature work directly to `master` or `develop`.

The repository's current production branch is named `master`, so Git Flow uses that name rather than renaming it to `main`. The Git Flow model is a branch convention; the `git-flow` helper executable is optional and is not required to use these branches.
