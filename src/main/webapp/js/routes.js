/* Demo Routes:

The first term must be the route from where the template will get the data.
The second term must be either a string representing the template name or
a function that returns the template name. The function will receive the json returned
by the request as the first parameter.
*/

$(document).ready(function() {
    router = Router({
        //"tester": function(json) { return json['isSupervisor'] ? "a" : "b";}
        // Use the last line to redirect unmatched routes to an error page
        "submission/:id": function(json) {
            json.binId = getRouteParams()[0];
            return "handins.submission.index";
        },

        "bin": "handins.bin.index"

        //"*undefined": "main.test"
    })
})


