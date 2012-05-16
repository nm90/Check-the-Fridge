from django.http import HttpResponse
from django.shortcuts import render_to_response
from django.contrib.auth import authenticate, login
from django.core import serializers
from django.template import RequestContext, Template
from django.core.context_processors import csrf
from fridge.models import Fridge, Item
from django.utils import simplejson
#from fridge.decorators import csrf_exempt
from django.views.decorators.csrf import csrf_exempt
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


@csrf_exempt
def fridge_login(request):
    if request.method == "POST":
        item_name = request.POST['name']
        item_amount = request.POST['amount']
        item_initamount = request.POST['initial_amount']
        item_fridge = request.POST['fridge_id']
        item_upc = request.POST['upc']

        item_to_save = Item(name=item_name, amount=item_amount, initial_amount=item_initamount, fridge=item_fridge, upc=item_upc)

        item_to_save.save()

        #['item_to_add']
        #response = json_item
        #t = Template("%s" % dir(request.POST))
        t = Template("%s" % item_name)

        context = RequestContext(request, {"": "", }, [ip_address_processor])

        return HttpResponse(t.render(context))
    elif request.method == "GET":
        """
	#response = HttpResponse(request, Template("%s" % getFridge(request),c))
        csrf_token = csrf.get_token(request)
        response = HttpResponse(simplejson.dumps({'csrf_token':csrf_token}), 'application/json')
        """

        t = Template("%s" % getFridge(request))

        context = RequestContext(request, {"": "", }, [ip_address_processor])

        return HttpResponse(t.render(context))



    t = Template("%s" % getFridge(request))

    context = RequestContext(request, {"": "", }, [ip_address_processor])

    return HttpResponse(t.render(context))


def search_form(request):
    return render_to_response('search_fridge.html')


"""
search for items with upc in the Bassett fridge
"""
def search_upc(request):
    response = HttpResponse()
    if 'q' in request.GET:
        bassett_items = Item.objects.filter(fridge__name='Bassett')
        items_with_upc = bassett_items.filter(upc=request.GET['q'])
        serializers.serialize("json", items_with_upc, stream=response)
        return response

    response = "Empty Search!"
    return response


"""

returns the posted_item on success
"""
@csrf_exempt
def update_item(request):
    response = HttpResponse()

    if request.method == "POST":
        item_name = request.POST['name']
        item_amount = int(request.POST['amount'])
        item_initamount = request.POST['initial_amount']
        item_fridge_id = request.POST['fridge_id']
        item_upc = request.POST['upc']

        fridge_obj = Fridge.objects.get(id=item_fridge_id)

        # if UPC code and Item pair already match add to
        # current amount
        if len(Item.objects.filter(fridge_id=item_fridge_id).filter(name=item_name)) == 0:
            item_to_save = Item(name=item_name, amount=item_amount, initial_amount=item_initamount, fridge=fridge_obj, upc=item_upc)
        else:
            item_to_save = Item.objects.get(name=item_name)
            if item_to_save.amount >= 0:
                item_to_save.amount += item_amount
            else:
                item_to_save.amount = item_amount
            item_to_save.initial_amount = item_amount

        item_to_save.save()

        posted_item = Item.objects.filter(fridge_id=item_fridge_id).filter(name=item_name)
        serializers.serialize("json", posted_item, stream=response)

    return response


"""
Search for items from the fridge with fridge_id passed as /q?=
"""
def search_id(request):

    if 'q' in request.GET:
        response = HttpResponse()
        serializers.serialize("json", Item.objects.filter(fridge__id=request.GET['q']).exclude(amount__lte=0), stream=response)
        return response

    response = "Empty Search"
    return response


"""
Search for items from the fridge with fridge.name passed as /q?=
"""
def search(request):

    if 'q' in request.GET:
        response = HttpResponse()
        serializers.serialize("json", Item.objects.filter(fridge__name=request.GET['q']).exclude(amount__lte=0), stream=response)
        return response

    response = "Empty Search!"
    return response


def contact(request):
 #   form = ContactForm(request.POST or None)
    c = {}
    c.update(csrf(request))

    return render_to_response('contact.html', form, c)
