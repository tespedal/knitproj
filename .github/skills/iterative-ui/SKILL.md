---
name: iterative-ui
description: Build or refine a user interface through short, reviewable iterations. Use when asked to create, improve, or visually iterate on an application's UI, including agent-driven or generative interfaces.
---

## Workflow

1. Inspect the existing application, UI framework, design conventions, and available run and test commands. Confirm the requested outcome and constraints; ask only about decisions that block a useful first iteration.
2. Identify the main user task and implement the smallest complete interface that supports it, using the existing stack and components.
3. Run the available checks and launch the application when possible. Inspect the rendered interface at relevant viewport sizes; fix functional, visual, and accessibility issues before presenting it.
4. Summarize what changed and what remains unverified. Invite focused feedback, then repeat from the affected part of the interface.

Keep each iteration coherent and easy to review. Preserve existing behavior outside the requested change.

## Agent-driven interfaces

First decide whether the user needs an agent to select or compose UI at runtime. A conventional, fixed interface is the default when that behavior is not requested.

When runtime-generated UI is needed, choose the most constrained pattern that satisfies the request:

- **Controlled:** The application owns predefined components; the agent selects one and supplies validated data. Prefer this for known interaction types and consistent product styling.
- **Declarative:** The agent supplies a structured UI description from a bounded component vocabulary; the application validates and renders it. Use this when users need combinations of known components without arbitrary executable UI.
- **Open-ended:** The agent supplies a largely complete UI surface. Choose this only when the user needs that flexibility and accepts the added security, consistency, and portability trade-offs.

AG-UI is an agent-to-application interaction protocol, not a UI pattern by itself. Use CopilotKit or another protocol/runtime only when it fits the existing stack and the requested behavior. Keep UI actions explicit, validate agent-provided data against an application-owned schema, and follow the host application's trust boundaries.

## References

- [CopilotKit Generative UI showcase](https://github.com/CopilotKit/CopilotKit/tree/main/examples/showcases/generative-ui) — patterns, trade-offs, and examples.
- [AG-UI protocol](https://github.com/ag-ui-protocol/ag-ui) — agent-to-application interaction layer.
