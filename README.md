# PushTemplates

## How to use: 

### 1. Add below line to the application class of your application. 

```
WebEngage.registerCustomPushRenderCallback(CustomCallback())
WebEngage.registerCustomPushRerenderCallback(CustomCallback())
```

### 2. Go to your webengage dashboard and add below key value pairs based on your template type.
###   - Progress Bar Template
| Key | Value | Use | 
| --- | --- | --- |
| `we_custom_render` | `true` | Tells the WebEngage SDK to use the custom renderer registered in the callbacks above.
| `template_type` | `bar` | **(required)**  Progress Bar template. 
| `future_time` | *epoch time in millis* | **(required)**  The time till which the notification is to be shown
| `timer_color` | *hex value* | The color for the countdown timer

###   - CountDown Template
| Key | Value | Use | 
| --- | --- | --- |
| `we_custom_render` | `true` | Tells the WebEngage SDK to use the custom renderer registered in the callbacks above.
| `template_type` | `timer` | **(required)**  Progress Bar template
| `future_time` | *epoch time in millis* | **(required)**  The time till which the notification is to be shown
| `timer_color` | *hex value* | The color for the countdown timer


<h4>
Sample Key Value Pairs: </h4>
<img width="633" alt="image" src="https://user-images.githubusercontent.com/88337360/184459615-693e2666-112a-4e9d-bab0-dc3c2482227b.png">

<h2>Progress Bar</h2>
<img src = "https://user-images.githubusercontent.com/88337360/184459762-19fabe48-8372-4aac-85df-985277ffafa8.png" width="500"/>


<h2>CountDown Timer</h2>
<img src = "https://user-images.githubusercontent.com/88337360/184459825-b86ba524-f8e1-4402-9cbf-5fd124acb2c6.png" width="500"/>
