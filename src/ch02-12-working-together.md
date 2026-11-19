# Working Together

A real robot's code isn't written by one person. A team of programmers works on it at once, all season: one adds a climber, another tunes the drivetrain, a third fixes a bug in autonomous, all in the same project. Without a way to keep track, people would overwrite each other's work, and nobody would know who changed what or how to get back yesterday's working version.
**Git** solves this. It's a program that records every change ever made to a project. Our team keeps its robot code on **GitHub**, a website that stores Git projects so everyone can share them.

## The words
Git comes with a lot of vocabulary. These are the words you need:
- A **repository**, or **repo**, is a project that Git keeps track of, along with its whole history.
- A **commit** is a saved snapshot of the project, with a message saying what changed. A repo's history is a long list of commits, and you can always look back at any of them or return to one.
- A **branch** is a separate line of commits, so you can work on something without changing anyone else's code. The main branch, the one everyone shares, is called **`main`**.
- **Cloning** copies a repo from GitHub to your laptop.
- **Pushing** sends your new commits up to GitHub. **Pulling** brings down everyone else's.
- A **pull request**, or **PR**, asks for the commits on your branch to be added to `main`. It's where other people review your code before it goes in.
- **Merging** adds one branch's commits into another, as happens when a pull request is accepted.

## Getting set up
You'll need a GitHub account. Make one at [github.com](https://github.com), then ask a mentor to add you to our team's GitHub organization so you can reach the robot code.
Git is already installed on the robotics laptops, and WPILib VS Code knows how to use it. The first time you commit, Git needs to know who you are so it can put your name on your commits. Open VS Code's terminal (**Ctrl+\`**) and type these two lines, with your name and the email address you used for GitHub:
```
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

### Cloning the robot code
1. On GitHub, go to our robot code's repo. Click the green **Code** button and copy the link it shows.
2. In VS Code, open the command palette and pick `Git: Clone`. Paste the link and press Enter.
3. Choose your `Documents` folder, and open the project when VS Code asks.

You now have all the robot code, and its entire history, on your laptop.

## Making a change
Every change goes through the same steps.

### 1. Start a branch
Never work directly on `main`. Everyone shares it, so it should only ever hold code that's been reviewed and works. Make a branch for each thing you work on instead.
Start from the newest code. The bottom-left corner of VS Code shows which branch you're on. Click it and pick `main`. Then open the **Source Control** panel (**Ctrl+Shift+G**), click the **...** menu at the top, and pick **Pull** to bring down everyone's latest commits.
Click the branch name in the bottom-left corner again and pick **Create new branch...**. Name the branch after what you're doing, in lowercase with dashes between words, like `arm-soft-limits` or `climb-commands`. That's the style our repo uses.

### 2. Make commits
Write your code as usual and test it in the simulator. Then open the **Source Control** panel, which lists every file you've changed. Click one to see exactly what changed: red lines were removed and green lines were added. This is a good moment to look over your own work before anyone else does.
When you're happy with a change:
1. Click the **+** next to each file you want to include. That's called **staging** it.
2. Type a message in the box at the top describing what you did.
3. Click **Commit**.

A good commit message says *what* changed in a few words, like "Add soft limits to arm" or "Fix intake not stopping". "Stuff" or "update" helps nobody, including you three weeks from now.
Commit often, each time you get one small thing working. Small commits are easy to understand and easy to undo if one turns out to be a mistake. Only commit code that builds.

### 3. Push
The first time, click **Publish Branch** in the Source Control panel to put your branch on GitHub. After that, whenever you have new commits, click **Sync Changes** to push them. Pushing often also keeps your work safe on GitHub if something happens to your laptop.

### 4. Open a pull request
When your change is finished and tested, go to the repo on GitHub. It usually shows a yellow banner about your branch with a **Compare & pull request** button. (If it doesn't, go to the **Pull requests** tab and click **New pull request**.) Check that it's asking to merge your branch into `main`.
Our repo fills in a description template. Complete it:
- **Issue number.** Our team tracks what needs doing as **issues** on GitHub, each with a number. If your PR finishes one, write `Closes #` and its number, like `Closes #42`, and GitHub will close the issue automatically when the PR is merged.
- **Comments.** Describe what you changed and how you tested it.

Then pick a teammate as **reviewer**, on the right side of the page. Our repo won't let a pull request be merged until someone else has reviewed it.

GitHub also builds every pull request automatically, the same way `WPILib: Build Robot Code` does. A green check means it built, and a red X means it didn't. Click **Details** next to the X to see the same errors VS Code would show you. A pull request with a red X can't be merged, so fix the errors, commit and push again.

### 5. Review
Your reviewer reads your changes and may leave comments with questions or suggestions. It isn't a test, and it isn't personal. Everyone's code gets reviewed, the mentors' included, because a second pair of eyes catches what the first pair missed.
To make a change, add new commits on the same branch and push them. The pull request updates on its own. Once your reviewer approves, it can be merged, and your code is part of the robot.

## Reviewing someone else's code
You'll review your teammates' code too. On a pull request, the **Files changed** tab shows every change. Click the **+** next to a line to comment on it.
What to look for, based on this book:
- **Does it build and was it tested?** The automatic check says whether it builds. The description should say how it was tested.
- **Constants.** Is every robot number a named constant, in the right constants class, with units?
- **IO.** Does all the hardware code live in an IO class, so the mechanism also works in the simulator?
- **Commands.** Is every command named with its mechanism, like `Arm.MoveTo`? Does every default command have the lowest priority? Do loops yield?
- **Safety.** Can anything make a mechanism move when it shouldn't, or keep moving forever? Is there a timeout where something could get stuck?
- **Can you understand it?** If you can't tell what some code is for, ask. If you're confused, the next person will be too.

Be kind and be specific. "This should be a constant in `ArmConstants`" is far more useful than "this is wrong." When something is done well, say so.

{{#quiz ../quizzes/ch02-12-git.toml}}

## When two people change the same thing
Git can usually combine everyone's changes by itself, even within one file. But if two branches change the *same lines* in different ways, Git can't know which version you want. That's a **merge conflict**.
To catch conflicts early, while they're small, bring `main`'s newest changes into your branch every so often. In the Source Control panel's **...** menu, pick **Branch**, then **Merge...**, and choose `main`.
When there's a conflict, VS Code shows the file with both versions marked:
```
<<<<<<< HEAD
	public static final double MAX_VOLTAGE = 4.0;
=======
	public static final double MAX_VOLTAGE = 6.0;
>>>>>>> main
```
The top part is yours, and the bottom part is what's on `main`. Above them, VS Code offers **Accept Current Change** (yours), **Accept Incoming Change** (theirs) and **Accept Both Changes**. Pick the right one, or edit the lines by hand until they're right, and make sure none of the `<<<<<<<`, `=======` or `>>>>>>>` lines are left. Then stage the file and commit.
If you're not sure which version is right, ask the person who made the other change. That always beats guessing.

## Try it yourself
Practice the whole process on your own project, where nothing can go wrong:
1. Open your `ArmRobot` project. In the **Source Control** panel, click **Publish to GitHub** and choose a **private** repository. That creates a repo for it on your own GitHub account, with everything you've written so far as the first commit.
2. Make a branch named after the change you're about to make, like `arm-soft-limits`.
3. Make a small change and commit it. If you haven't done the soft limits from [Real Hardware](ch02-11-real-hardware.md) yet, they're a perfect fit. Otherwise anything small works, like changing a constant.
4. Push your branch and open a pull request on GitHub into your own `main`.
5. Look through the **Files changed** tab as a reviewer would. Then click **Merge pull request**.
6. Back in VS Code, switch to `main` and pull. Your change is there.

<details>
<summary>What it should look like</summary>

When you're done, your repo on GitHub has three commits on `main`: the first one with your whole project, the one from your pull request, and a "merge" commit that GitHub made when you merged it. The **Pull requests** tab shows your pull request as **Merged**, in purple.
If **Publish to GitHub** asks you to sign in, go ahead. It opens a browser window to connect VS Code to your GitHub account. If you get stuck anywhere, ask a mentor. Git confuses everyone at first, and the only way to get comfortable with it is to use it a lot.

</details>
