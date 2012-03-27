from django.db import models

class Fridge(models.Model):
    name = models.CharField( max_length=50 )

    def __unicode__(self):
        return (self.name + " Fridge"+ (" : %d" % self.id))

class Item(models.Model):
    name = models.CharField(\
            primary_key=True,\
            max_length=50
            )

    amount = models.PositiveIntegerField( default=1 )

    fridge = models.ForeignKey(Fridge)

    unique_together = ("name", "fridge")

    def __unicode__(self):
        return (self.name + ( " : %d" % self.amount) )

