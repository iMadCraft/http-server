package com.kskb.se.http;

public interface ApiTemplates {
   String SYSTEM_INFO = """
      <html>
      <body>
           <table>
               <tr>
                   <td>Operating System</td>
                   <td>{{ os }}</td>
               </tr>
               <tr>
                   <td>Java</td>
                   <td>{{ java_version }}</td>
               </tr>
           </table>
      </body>
      </html>
      """;
}
