<p align="center"> 
    <img src="https://github.com/coochill/GeoMinder/blob/main/assets/500x500.png" width="300">
</p>

<h1 align="center"> GeoMinder: Location-based Task Reminder</h1>
<h3 align="center"> CS312 - Mobile Computing</h3>
<h5 align="center"> Final Project - Batangas State University - Alangilan, 3rd Year, 1st Semester 2024 </h5>

<h5 align="center"> Members </h5>
<p align="center">De Leon, Christine Joyce C.</p>
<p align="center">Domingo, Joy Susette V.</p>
<p align="center">Sebastian, Phoemela Kyle M.</p>

---

## 📑 Table of Contents
1. [About](#about)  
2. [Features](#features)  
   - [Landing Page](#landing-page)  
   - [About Page](#about-page)  
   - [Login and Sign Up](#login-and-sign-up)  
   - [Notifications and Location Permission](#notifications-and-location-permission)  
   - [Profile](#profile)  
   - [History](#history)  
   - [Input Task and On-Click Notification](#input-task-and-on-click-notification)  
3. [Mobile Computing Applications](#mobile-computing-applications)  
4. [📚 Tools and Technologies Used](#-tools-and-technologies-used)  
5. [Use Cases](#use-cases)  
6. [Future Improvements](#future-improvements)  

---

## About
<p><strong>Your Tasks, Right Place at the Right Time</strong></p>
<p>A location-based task reminder app that helps you stay productive by sending reminders based on your location! Whether it's a grocery list at the store or a meeting reminder at the office, GeoMinder ensures you never miss a task.</p>

---

## Features

### Landing Page
<table>
  <tr>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Landing1.jpg" width="200"></td>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Landing2.jpg" width="200"></td>
  </tr>
  <tr>
    <td align="center">Tagline and Features to Explore</td>
    <td align="center">Button to About page</td>
  </tr>
</table>

### About Page
<table>
  <tr>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/About.jpg" width="200"></td>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/About2.jpg" width="200"></td>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Reminder.jpg" width="200"></td>
  </tr>
  <tr>
    <td align="center">Brief details of GeoMinder</td>
    <td align="center">Instructions & button to Sign Up</td>
    <td align="center">Reminder Dialog</td>
  </tr>
</table>

### Login and Sign Up
<table>
  <tr>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/login.jpg" width="200"></td>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/signup.jpg" width="200"></td>
  </tr>
  <tr>
    <td align="center">Login</td>
    <td align="center">Sign Up</td>
  </tr>
</table>

### Notifications and Location Permission
<table>
  <tr>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Locationpermission.jpg" width="200"></td>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Location%20Permission.jpg" width="200"></td>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Permission.jpg" width="200"></td>
  </tr>
  <tr>
    <td align="center">Location Permission pop-up</td>
    <td align="center">Location Permission Settings</td>
    <td align="center">Allowing Notification and Location</td>
  </tr>
</table>

### Profile
<table>
  <tr>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Profile.jpg" width="200"></td>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Inputted%20taskdetails.jpg" width="200"></td>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Delete%20Task.jpg" width="200"></td>
  </tr>
  <tr>
    <td align="center">Profile Page</td>
    <td align="center">Inputted Task Details</td>
    <td align="center">Delete Task</td>
  </tr>
</table>

### History
<table>
  <tr>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/History.jpg" width="200"></td>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/History%20Deleted.jpg" width="200"></td>
  </tr>
  <tr>
    <td align="center">Completed</td>
    <td align="center">Deleted</td>
  </tr>
</table>

### Input Task And On-Click Notification (Decision Page)
<table>
  <tr>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Input%20Page.jpg" width="200"></td>  
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Notif.jpg" width="200"></td>
    <td><img src="https://github.com/coochill/GeoMinder/blob/main/assets/Decision.jpg" width="200"></td>
  </tr>
  <tr>
    <td align="center">Input task Page</td>  
    <td align="center">Notification</td>
    <td align="center">Decision Page</td>
  </tr>
</table>

---

## Mobile Computing Applications
<ul>
  <li><strong>Task Management</strong>: GeoMinder allows users to manage their tasks based on location triggers.</li>
  <li><strong>Location-Based Reminders</strong>: Sends reminders when the user enters or leaves a specific location.</li>
  <li><strong>Google Maps Integration</strong>: Provides real-time mapping and location services for seamless navigation and task monitoring.</li>
  <li><strong>Firebase Notifications</strong>: Ensures reliable push notifications for task reminders and updates.</li>
</ul>

---

## 📚 Tools and Technologies Used

<ul>
  <li><b>🖥️ IDE</b>: Android Studio</li>
  <li><b>🛠 Plugins</b>: Google Services and Android Application</li>
  <li><b>📦 Libraries</b>: Firebase, Google Places API, AndroidSVG, GeoFencing and OkHttp</li>
</ul>

### Technical Architecture
- **Backend**: Firebase Firestore for storing tasks and managing reminders.  
- **Geofencing**: Google Places API to detect user locations and trigger tasks.  
- **Notifications**: Firebase Cloud Messaging ensures reliable delivery of push notifications.  
- **Frontend**: Built using Android Studio with Material Design principles for UI/UX.  
- **Networking**: OkHttp for efficient API communication.  

---

## Use Cases

- **Shopping List Reminders**  
  Set reminders for your grocery list that only notify you when you arrive at the supermarket.  

- **Office Task Alerts**  
  Never forget an important work task by scheduling reminders for when you reach the office.  

- **Errands and Chores**  
  Automatically get reminded to pick up your laundry or drop off a package when near the respective locations.  

- **Travel Preparations**  
  Plan your travel checklist and get notified as you approach specific locations, like the airport or hotel.  

---

## Future Improvements

- **Recurring Tasks**: Allow users to set tasks that repeat daily, weekly, or monthly.  
- **AI-based Suggestions**: Use AI to recommend task locations based on user habits and history.  
- **Voice Integration**: Add voice commands to create and manage tasks easily.  
- **Cross-Platform Support**: Expand the app to iOS and web platforms for a wider audience.  
- **Collaboration Features**: Enable sharing and assigning tasks to friends or colleagues.  
- **Dark Mode Support**: Enhance accessibility and user experience with a dark mode option.  

---
