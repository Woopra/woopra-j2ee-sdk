Track customers directly in Java using Woopra's Java SDK

The SDK can be used both for front-end and back-end tracking. In either cases, you should setup the tracker SDK first. To do so, configure the tracker instance as follows (replace <code>mybusiness.com</code> with your website as registered on Woopra, and specify as parameters the instances of your Servlet's HttpServletRequest and HttpServletResponse):

``` java
WoopraTracker woopra = new WoopraTracker(request, response);
woopra.config(WoopraTracker.DOMAIN, "mybusiness.com");
```

You can update your idle timeout (default: 30 seconds) by updating the timeout property in your <code>WoopraTracker</code> instance:

``` java
woopra.config(WoopraTracker.IDLE_TIMEOUT, 15000); // in milliseconds
```

If you don't want to keep the user online on Woopra when they don't commit any event between the last event and the <code>IDLE_TIMEOUT</code>, you can disable auto pings (auto ping only matters for front-end tracking).

``` java
woopra.config(WoopraTracker.PING, false); // default is true
```

Configuration could also have been done in one single step, by adding all the properties you wish to configure to a 2D Array:

``` java
woopra.config(new Object[][] {
   {WoopraTracker.DOMAIN, "4ltrophy.campus.ecp.fr"},
   {WoopraTracker.IDLE_TIMEOUT, 15000},
   {WoopraTracker.PING, false},
});
```

To add custom visitor properties, you should pass a 2D Array to the <code>identify(String[][] user)</code> function:

``` java
woopra.identify(new String[][] {
   {"name", "Antoine"},
   {"email", "antoine@woopra.com"},
   {"company", "My Business"}
});
```

If you wish to track page views, first call <code>track()</code>, and finally calling <code>woopraCode()</code> in your page header will insert the woopra javascript tracker:

``` java
<head>
   ...
  <% woopra.track().woopraCode(); %>
</head>
```

You can always track events through front-end later in the page. With all the previous steps done at once, it should look like:

``` java
<html>
   <head>
      ...
      <%
         woopra = new WoopraTracker(request, response);
         woopra.config(WoopraTracker.DOMAIN, "mybusiness.com").identify(user).track().woopraCode();
      %>
   </head>
   <body>
      ...
      <%
         event = new WoopraEvent("play");
         event.setProperty("artist", "Dave Brubeck");
         event.setProperty("song", "Take Five");
         event.setProperty("genre", "Jazz");
         woopra.track(event);
      %>
      ...

   </body>
</html>
```

To track a custom event through back-end, just specify the additional parameter <code>true</code> in the <code>track()</code> functions.

``` java
woopra.track(event, true);
```

If you prefer, you can also track an event without even having to create a WoopraEvent Object:

``` java
woopra.track("play", new Object[][] {
   {"artist", "Dave Brubeck"},
   {"song", "Take Five"},
   {"genre", "Jazz"},
}, true);
```

If you identify the user after the last tracking event, don't forget to <code>push()</code> the update to Woopra:

``` java
woopra.identify(user).push();

//or, to push through back-end:
woopra.identify(user).push(true);
```

If you're only going to be tracking through the back-end, set the cookie (before the headers are sent):

``` java
woopra.setWoopraCookie();
```
