# AraLinks demo kit

Everything the web demo (`demo/index.html`) uses, exported so the JavaFX app (or slides, or Figma) can reuse it.

## mascot/

Sparty in each mood, as SVG (editable) and PNG (512×589, transparent). The demo shows them here:

| File | Where it appears |
|---|---|
| `sparty-waving` | Sidebar logo, login logo, login hero |
| `sparty-cheering` | Celebration screen (request sent, class published, session complete) |
| `sparty-pointing` | Tip bubble, "Tap Rate" panel, tutor empty states |
| `sparty-in-love` | "You're in!" after joining a class |
| `sparty-happy` | "Request sent" card, default |
| `sparty-worried` | Leave / remove / cancel confirm dialogs, searches with no results |
| `sparty-sleepy` | "No classes yet", "No sessions yet" |
| `sparty-fired-up` | "Want to teach too?" card |
| `sparty-surprised` | Not used yet (spare for alerts) |

`mascot-sheet.png` shows them all side by side.

The SVGs use blur filters for the eye glow, shine and shadow. Some tools (including Figma and JavaFX's own SVG support) drop or flatten blur filters, so use the PNGs when the glow matters.

## logo/

App icon (tilted and peeking versions), logo with Sparty, and the wordmark. The wordmark letters are vector outlines, so no font is needed to display them.

## theme.css / theme.json

The color tokens from the demo. `brand` is Sparty red (buttons, logo, active menu item); `green` is only for success; `red` is only for errors. The font is Nunito 600–900 from Google Fonts.

## data/

- `courses.json`: the eight demo courses with their icon and card color.
- `seed-data.json`: the demo's users, classes, requests, reviews, XP and streaks. A good starting point for the JavaFX `DataStore` seed. Demo accounts all use the password `demo123`; it is for local demos only.
