from django.http import HttpResponse
from django.contrib.auth import authenticate, login
from django.core import serializers
from django.template import RequestContext, Template

from fridge.models import Fridge

import datetime


def current_datetime(request):
    now = datetime.datetime.now()
    html = "<html><body>It is now %s.</body></html>" % now
    return HttpResponse(html)


def getFridge(request):
    if request.method == 'GET':
        response_data = {
                "fridge": "",
                "item": ""
                }
        response_data['fridge'] = 'TestFridge Does Not Exist'
        if request.user.is_authenticated():

            JSONSerializer = serializers.get_serializer("json")
            json_serializer = JSONSerializer()
            response = HttpResponse()
            # serialize model into the response object
            json_serializer.serialize(Fridge.objects.all(), ensure_ascii=False, stream=response)
        else:   # redirect to login
            response = HttpResponse("Failure")
        return response


def ip_address_processor(request):
    return {'ip_address': request.META['REMOTE_ADDR']}


def fridge_login(request):
    if request.method == "PUT":
        #login here
        usernm = request.data['owner_name']
        passwd = request.data['fridge_id']
        user = authenticate(username=usernm, password=passwd)
        if user is not None:
            if user.is_active:
                login(request, user)
                # Redirect to success page
            else:
                # Return 'disabled accnt' error msg
                pass
        else:
            # Return an 'invalid login' error msg
            pass

    t = Template("%s" % getFridge(request))

    context = RequestContext(request, {"": "", }, [ip_address_processor])

    return HttpResponse(t.render(context))
