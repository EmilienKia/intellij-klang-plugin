# Skills

This folder contains **skills** — repeatable, step-by-step playbooks for recurring tasks in
this repository. They are written primarily for LLM coding agents (and humans) so a task can
be executed consistently without rediscovering the procedure each time.

Each skill lives in its own subfolder as a `SKILL.md` file:

```
.agents/skills/
├── README.md                  # this file
└── update-k-grammar/
    └── SKILL.md               # sync the K grammar from klang.ebnf into the plugin
```

## Conventions

- One subfolder per skill; the entry point is always `SKILL.md`.
- Start each `SKILL.md` with a short **front-matter** block (`name`, `description`,
  `when to use`) so it can be selected quickly.
- Keep instructions concrete: exact commands, file paths and acceptance criteria.
- Skills should reference, not duplicate, the project conventions in
  [`../../AGENT.md`](../../AGENT.md).

## Available skills

| Skill | Purpose |
|---|---|
| [`update-k-grammar`](update-k-grammar/SKILL.md) | Propagate changes from an updated `klang.ebnf` into `klang.bnf` / `klang.flex`, regenerate, and wire up lexing → highlighting (with semantic follow-ups recorded in `TODO.md`). |

