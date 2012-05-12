from django.http import HttpResponse
from django.shortcuts import render_to_response
from django.contrib.auth import authenticate, login
from django.core import serializers
from django.template import RequestContext, Template
from django.core.context_processors import csrf
from fridge.models import Fridge, Item
from django.utils import simplejson
#from fridge.forms import ContactForm

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
            response = HttpResponse(json_serializer.serialize(Fridge.objects.all(), ensure_ascii=False, stream=response))
        else:   # redirect to login
            response = HttpResponse("Failure")

    return response


def ip_address_processor(request):
    return {'ip_address': request.META['REMOTE_ADDR']}

def fridge_login(request):
    if request.method == "POST":
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
    elif request.method == "GET":
        c = {}
	#response = HttpResponse(request, Template("%s" % getFridge(request),c))
        csrf_token = csrf.get_token(request)
        response = HttpResponse(simplejson.dumps({'csrf_token':csrf_token}), 'application/json')

        return response


    t = Template("%s" % getFridge(request))

    context = RequestContext(request, {"": "", }, [ip_address_processor])

    return HttpResponse(t.render(context))


def search_form(request):
    return render_to_response('search_fridge.html')

def search(request):
    if 'q' in request.GET:
        response = HttpResponse()
        serializers.serialize("json", Item.objects.filter(fridge__name=request.GET['q']), stream=response)
        return response

    response = "Empty Search!"
    return response

def contact(request):
 #   form = ContactForm(request.POST or None)
    c = {}
    c.update(csrf(request))

    return render_to_response('contact.html', form, c)
