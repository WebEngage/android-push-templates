# PushTemplates

## How to use: 

### 1. Download the module from the github and add the module as a dependency in your build.gradle file of you application.

### 2. Follow the guide provided <a href="https://docs.webengage.com/docs/android-customizing-push-notifications">here</a> for customizing push notifications to create your own templates as required.

### 3. Add below line to the application class of your application or edit your callbacks for including the callbacks provided in the module.

```
WebEngage.registerCustomPushRenderCallback(CustomCallback())
WebEngage.registerCustomPushRerenderCallback(CustomCallback())
```

### 4. Go to your webengage dashboard and add below key value pairs to your push campaign based on your template type.
###   - Progress Bar Template
| Key | Value | Use | 
| --- | --- | --- |
| `we_custom_render` | `true` | Tells the WebEngage SDK to use the custom renderer registered in the callbacks above.
| `template_type` | `bar` | **(required)**  Progress Bar template. 
| `future_time` | *epoch time in millis* | **(required)**  The time till which the notification is to be shown
| `timer_color` | *hex value* | The color for the countdown timer
| `show_dismiss_cta` | `true` / `false` | Show the Dismiss button at end of the CTA list


###   - CountDown Template
| Key | Value | Use | 
| --- | --- | --- |
| `we_custom_render` | `true` | Tells the WebEngage SDK to use the custom renderer registered in the callbacks above.
| `template_type` | `timer` | **(required)** CountDown Timer template
| `future_time` | *epoch time in millis* | **(required)**  The time till which the notification is to be shown
| `timer_color` | *hex value* | The color for the countdown timer
| `show_dismiss_cta` | `true` / `false` | Show the Dismiss button at end of the CTA list



<h4>
Sample Key Value Pairs: </h4>
<img width="633" alt="image" src="https://user-images.githubusercontent.com/88337360/184459615-693e2666-112a-4e9d-bab0-dc3c2482227b.png">

<h2>Progress Bar</h2>
<img src = "https://user-images.githubusercontent.com/88337360/191495313-df29a1e1-f8f8-49ef-a3f1-2cb59cb00ba9.gif" width="500"/>


<h2>CountDown Timer</h2>
<img src = "https://user-images.githubusercontent.com/88337360/191495355-d64fcf75-12e9-47b4-9ba9-3bee47b6e6d9.gif" width="500"/>
