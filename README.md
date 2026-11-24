# Team Project

***Please keep this up-to-date with information about your project throughout the term.

The readme should include information such as:
- a summary of what your application is all about
- a list of the user stories, along with who is responsible for each one
- information about the API(s) that your project uses 
- screenshots or animations demonstrating current functionality

By keeping this README up-to-date,
your team will find it easier to prepare for the final presentation
at the end of the term.***
---

# Team Members

- Luke Le
- Daniel Yang
- Tristan Cao
- Jessy Mclellan-Wang

---

# Idea 
- fashion discovery platform 
- similar to pinterest, but for fashion (or like the roblox marketplace).
- clothing would include tops, bottoms, footwear, accessories (rings, necklaces, bracelets, etc..), hats, bags, and more.

---

# Domain
## Domain description
This project addresses the domain of **Fashion**.
### Core Entities:

- Item: Represents a piece of clothing
- ...

### Critical Business Rules:

- ...
- ...

## Main Features: 
- browse + scroll through a variety of fashion items from different brands
- filter clothing items by category, price range, size, brand, and style (i.e, color, pattern, casual, formal, etc.)
- search bar (mainly by name)
- save items which can be viewed in a different section

extra features (potential, not required for MVP): 
- image search
- login feature (important for unlocking more features)
- get personalized recommendations based on saved items
- AI outfit builder based on saved items 
  - add filters to generated outfits for themes and price ranges
- create mood boards/collections of saved items
- item description page with more details about the item, including price, brand, and similar items

## Software Design & Architecture
This application follows **Clean Architecture** principles to ensure high cohesion and low coupling.

- Entities: Located in the innermost layer, containing the core business logic described above.
- Use Cases: Coordinate the flow of data to and from entities to achieve system goals.
---
# User Stories
## User story 1

- As a fashionista, I want to find accessories that go with my outfit
## User story 2

- As a student, I want to make a shopping list within my budget
## User story 3

- As a white-collar worker, I want to look for formal clothing
## User story 4

- As a user, I want to be able to sign up and login to have access to my saved items from different devices

---
# Use cases  

## Use case: Browse items

**Primary actor:** User 

**Precondition:** 

- The system contains at least one item to display.
- The user is on the discovery or catalog screen.

**Trigger:** the user requests to view available items

**Success Outcome:** The user has viewed a list of items and can see their basic details. 

**Main Success Scenario:**

1. The user asks the system to show available items.

2. The system retrieves a collection of items.

3. The system displays the first set of items to the user.

4. The user asks to see more items (e.g., by scrolling or paging).

5. The system retrieves and displays the next set of items.



The user repeats steps 4-5 until they find an item of interest or stop browsing.

**Extensions:**

2a. No items are found in the system.

The system informs the user that no items are currently available.

The use case ends.

5a. The system cannot retrieve more items due to a connection error.

The system alerts the user to the error and suggests retrying.

## Use case: Search for Item

**Primary Actor:** User

**Preconditions:**

The user is on the search or browse screen.

**Trigger:** The user submits a search query (e.g., enters a keyword or selects filters).

**Success Outcome:** The system displays a list of items that match the provided criteria.

**Main Success Scenario:**

1. The user provides search criteria (keywords, properties, or sort order).

2. The system validates the search criteria.

3. The system searches the item catalog for matches.

4. The system retrieves the matching items.

5. The system presents the list of matching items to the user.

**Extensions:**

3a. No items match the criteria.

The system informs the user that no results were found.

The system suggests broadening the search criteria.

The use case ends.

2a. The search criteria are invalid (e.g., empty keyword when required).

The system alerts the user to the invalid input.

The use case returns to step 1.

[//]: # (Logistics:)

[//]: # (- brose feature &#40;main interface&#41;)

[//]: # (  - a)

[//]: # (- filtering feature &#40;for browsing&#41; )

[//]: # (  - a)

[//]: # (- search feature &#40;for browsing&#41; )

[//]: # (  - a)

[//]: # (- save feature &#40;for saving items&#41; )

[//]: # (  - a)