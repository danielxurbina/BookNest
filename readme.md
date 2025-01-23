# Milestone 1 - BookNest(Unit 7)

## Table of Contents

1. [Overview](#Overview)
2. [Product Spec](#Product-Spec)
3. [Wireframes](#Wireframes)

## Overview
### Description

BookNest allows users to discover new books, manage a personalized reading list, and track their reading progress. By integrating the Google Books API, users can search for books, view detailed information, and organize their reading goals in one place.

### App Evaluation
- **Description:**  
	- The app helps users discover and track books through a personalized library, offering detailed book information and progress tracking to enhance their reading habits.
- **Category:**  
	- Entertainment / Productivity
- **Mobile:**  
	- Fetches book data in real time via the Google Books API.  
	- Provides a seamless experience for managing and updating a reading list on the go.  
	- Push notifications remind users to read or update their progress.  
- **Story:**  
	- The app makes reading more engaging and organized, helping users discover books and track their progress effortlessly.  
	- Peers and book enthusiasts will find the app practical and appealing, promoting regular use and engagement.  
- **Market:**  
	- Targets avid readers, students, and professionals who aim to enhance and organize their reading habits.  
	- Appeals to book lovers looking for a streamlined solution for managing their reading lists.  
- **Habit:**  
	- Encourages daily interactions as users log their reading progress and explore new books.  
	- Promotes engagement through progress tracking and personalized book recommendations.  
- **Scope:**  
	- **Technical Challenge:**  
	    - Integrating the Google Books API for book searches and fetching detailed information is achievable within the program's timeline.  
	    - Implementing reading progress tracking and a personalized library adds an exciting technical challenge.  
	- **Stripped-Down Version:**  
	    - A version with basic book search, detailed views, and personal library management is functional and valuable.  
	    - Advanced features, such as reading statistics and push notifications, can be introduced in later iterations.  
	- **Product Clarity:**  
	    - The app’s purpose is well-defined: to help users discover, organize, and track their reading habits.  
	    - Each feature aligns with the goal of simplifying and enhancing the reading experience for users.  

---
## Product Spec

### 1. User Features (Required and Optional)

**Required Features**
- [x] User can login/logout.  
- [x] User can sign up for a new account.
- [x] User can search for books using the Google Books API.  
- [x] User can set reading goals.  
- [x] User can add books to their reading list and categorize them as "To Read," "In Progress," or "Completed."  
- [x] User can update reading progress (e.g., number of pages read).  

**Optional Features**
- [x] Push notifications to remind users to read or update progress.  

---
### 2. Screen Archetypes
- **Login Screen:**  
  - Allows users to log in with their credentials or navigate to the sign-up screen.  
- **Signup Screen:**  
  - Enables new users to create an account.  
- **Home Screen:**  
  - Displays the user's reading list with sections for "To Read," "In Progress," and "Completed."  
- **Search Screen:**  
  - Allows users to search for books by title, author, or genre.  
- **Book Details Screen:**  
  - Shows detailed information about a selected book, including description, author, and cover image.  
- **Progress Tracker Screen:**  
  - Allows users to update their reading progress and view their current streaks or milestones.  

---
### 3. Navigation

**Tab Navigation** (Tab to Screen)
* Home Tab → Displays the user's reading list.  
* Search Tab → Allows users to search for books.  
* Progress Tab → Displays and updates reading progress.  

**Flow Navigation** (Screen to Screen)
- Login Screen → Home Screen (upon successful login).  
- Signup Screen → Home Screen (upon successful signup).  
- Home Screen → Book Details Screen → Progress Tracker Screen.
- Search Screen → Book Details Screen → Add to Reading List.  

---

## Wireframes
![Imgur Image](https://github.com/user-attachments/assets/9ead93db-e5c0-41e6-8d01-267e5a32db49)

<br>

---

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

---

# Milestone 2 - Build Sprint 1 (Unit 8)

## GitHub Project Board
<img width="600" alt="Screenshot 2024-11-29 at 5 02 46 PM" src="https://github.com/user-attachments/assets/fa4c1231-e392-4f76-b9db-d9b33b70dfe1">

---

## Issue Cards
- <img width="600" alt="Screenshot 2024-11-29 at 4 57 41 PM" src="https://github.com/user-attachments/assets/d5800bf1-b18d-4e0b-9e3d-155d2525a412">

- <img width="600" alt="Screenshot 2024-11-29 at 5 27 57 PM" src="https://github.com/user-attachments/assets/f47ddd0a-872e-46ac-a64e-9721ce45fcae">


---

## Issues Worked on This Sprint

- Initialize Android Project
- Design Login Screen UI
- Design Signup Screen UI
- Implement Navigation Between Login and Signup Screens
- Implement Firebase Signup Functionality
- Implement Firebase Login Functionality
- Create Tab Bar Controller with 3 Tabs
- Create Placeholder Home Screen Layout
- Create Placeholder Search Screen Layout
- Create Placeholder Progress Screen Layout
- Set Up Google Books API Integration
- Handle Firebase Authentication State Persistence
- Implement Search Functionality

<img src="https://github.com/user-attachments/assets/e57e3d1c-fca2-4f28-8867-a4ecbca11bf8" width=250>

---

# Milestone 3 - Build Sprint 2 (Unit 9)

## GitHub Project Board
<img src="https://github.com/user-attachments/assets/8a7b57df-21e9-41d1-a0f2-607b8dddbc5e" width=600>

---

## Completed User Stories
- [x] User can set reading goals.  
- [x] User can add books to their reading list and categorize them as "To Read," "In Progress," or "Completed."  
- [x] User can update reading progress (e.g., number of pages read).
- [x] Push notifications to remind users to read or update progress.
      
- I replaced the graph features with the reading goals functionality, as there wasn't enough data available to create meaningful visual representation on the graph.

<img src="https://github.com/user-attachments/assets/f2140a2e-ae35-4e2b-b788-26f6c9bdef23" width=250>

---

## App Demo Video
[![Watch the video](https://img.youtube.com/vi/5bfq0tvTibM/0.jpg)](https://www.youtube.com/watch?v=5bfq0tvTibM)
