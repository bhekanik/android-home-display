# Add the starter dashboard to Home Assistant

Use [dashboard.yaml](dashboard.yaml) as a new dashboard. It contains a clock, weather, a calendar and room controls, all using built-in cards. No HACS installation, custom integration or API token is needed.

1. Connect your devices and calendar to Home Assistant first. Find their entity IDs in Settings → Devices & services → Entities.
2. Copy `dashboard.yaml` into a local editor. Replace `weather.example_home`, `calendar.example_calendar`, `light.example_light` and `climate.example_thermostat` with your entity IDs. Remove any whole card you don't need. The weather entity must provide daily forecasts.
3. In Settings → Dashboards, add a new dashboard from scratch. Keep it available to non-admin users if the tablet will use one. Open the dashboard, enter edit mode, then choose the raw configuration editor from the menu.
4. Paste the complete edited YAML into that new dashboard and save. Do not paste it into Home Assistant's `configuration.yaml` or replace an existing dashboard you want to keep.
5. Confirm the cards show your data and try the controls deliberately. A thermostat card controls the real thermostat after you map its entity.
6. Copy the dashboard's URL into the Android app. Sign in on the tablet with a separate non-administrator Home Assistant user and retain the session.

Unchanged example IDs produce missing-entity cards. They are placeholders, not demo devices. A non-administrator account still has control permissions; this template does not provide per-device access restrictions.

See Home Assistant's [dashboard guide](https://www.home-assistant.io/dashboards/dashboards/), [calendar card](https://www.home-assistant.io/dashboards/calendar/) and [weather card](https://www.home-assistant.io/dashboards/weather-forecast/) for current options.

The YAML only configures the view. It does not install calendar integrations, provision credentials, create automations or add a screensaver. Change layouts and cards in Home Assistant without rebuilding the Android app.
