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

    initial_amount = models.PositiveIntegerField( default=1 )

    fridge = models.ForeignKey(Fridge)

    upc = models.CharField( max_length=15 )

    unique_together = ("name", "fridge", "upc")

    def __unicode__(self):
        return (self.name + ( " : %d" % self.amount) + ( " : %s" % self.upc))

    def save(self, *args, **kwargs):
        super(Item, self).save(*args, **kwargs)
        """
        self.name = kwargs['name']
        self.amount = kwargs['amount']
        self.initial_amount = kwargs['initial_amount']
        self.fridge = kwargs['fridge']
        self.upc = kwargs['upc']
        return
        """


