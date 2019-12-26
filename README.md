# Harmony
<p align="center">
  <img src="https://github.com/kenny2892/Harmony/blob/master/Harmony/src/resources/Icon.png" width="90">
  <img src="https://github.com/kenny2892/Harmony/blob/master/Harmony/src/resources/Icon%20Text%20-%20Orange.png" width="500">
</p>

This is the Harmony Project. What was supposed to be a simple chat app for my Computer Networks class turned into me trying to recreate [Discord](https://discordapp.com/).

<p align="center">
  <img src="https://i.gyazo.com/3c9ef8ca6776a51b6410e2baa2b1120d.png" width="900">
</p>

To use Harmony you have to have a server running and then any users can connnect to the given ip and port number. Each user can log-in with a username and password (the server does not allow for duplicate usernames to log-in) and then you can select a user icon, user color, and downloads folder.

From there, you can join one of the three rooms by clicking on their icons on the side.

Inside the room, you can use various commands:
- \x : Close the Application (you can also just hit the X in the upper right corner).
- \l : Have the server message you a list of the users in your room (you can just look to the right side of the chat to see who is in the room).
- \r : Have the server message you a list of the room names (you can just look at the room icons).
- \e (room name) : Move to a specified room (you can just click on the room icon).
- \w : Have the server message you what room you are currently in (you can just look at the left side and see where the white bar is).
- \p (username) : Enter a private chat with a specific user.
  - This means that you will only recieve messages from this user and not the rest of the room, but that user can still send and recieve messages from and to others unless they have entered a private chat with you.
  - Ex: 
    - I join room One and enter private mode with "user1". 
    - "user1" sends a message to the room and everyone, myself included, sees it.
    - "user2" sends a message to the room and everyone but me sees it.
- \q : Leave private mode.
- \msg (username) : Send a message to a specific person in the room.

*Note: The reason there are many redundant commands is because the app had to be compatible with other student's apps, many of which were purely console based so the commands were necessary.*

In addition, you can send files to other users by clicking the "+" next to the where you type. This has no limit on file size, but it is recommended that you only do it with small files as to ensure nothing goes wrong.
