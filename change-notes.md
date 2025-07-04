# Vespa-YQL-Plugin Change Notes

Latest Version: **1.0.6**

Built at: **2025-07-04 11:43:00 CEST**

<ul class="incremental">
  <li>[1.0.6] - Intellij Marketplace fixes
      <ul class="incremental">
        <li>Script to generate change notes.</li>
        <li>Fixed version compability values for marketplace.</li>
      </ul>
  </li>
  <li>[1.0.5] - Minor changes
      <ul class="incremental">
        <li>Added link in panel to cluster controller.</li>
        <li>Added config value for cluster controller endpoint.</li>
        <li>Removed zipkin browser panel and changed it to a upload and open browser button.</li>
      </ul>
  </li>
  <li>[1.0.4] - Build script update to support 2024+ version of intellij</li>
  <li>[1.0.3] - Support for Vespa Java Apps
    <ul class="incremental">
      <li>Package, prepare and activate will call maven before packaging and uploading the application.</li>
      <li>The Vespa tool windows (except for the Side panel) will be hidden from start until they are needed.</li>
      <li>New tool window for Vespa Cluster logs
        <ul class="incremental">
          <li>Works with log files/dirs mapped from docker containers (<code>vespa.log</code> or <code>logarchive</code> directory).</li>
      </ul></li>
  </ul></li>
  <li>[1.0.2] - TLS and simple upload
    <ul class="incremental">
      <li>TLS support for connections (not tested on vespa-cloud as I do not have a vespa-cloud)</li>
      <li>Right click on application dir and select “Package, Prepare and Activate”
        <ul class="incremental">
          <li>Onlys zips the dir and calls the prepare and activate function on the config endpoint</li>
          <li>No support for application code for now.</li>
      </ul></li>
      <li>Simple visualization of service.xml files
        <ul class="incremental">
          <li>Right-click on a services.xml file and select “Show Service Overview”</li>
      </ul></li>
      <li>Show connection status in the Vespa dock</li>
      <li>Bugfix for Vespa Results toolWindow
        <ul class="incremental">
          <li>Responses will now turn up on first execution</li>
      </ul></li>
  </ul></li>
  <li>[1.0.1] - Fix since-build for idea-version</li>
  <li>[1.0.0] - First version</li>
</ul>