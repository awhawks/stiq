#include <Python.h>
#include "structmember.h"

typedef struct{
    PyObject_HEAD
    PyObject *first;
    PyObject *last;
    int number;
    /* Type-specific fields go here.*/
}Noddy;


/* destructor
 */
static void
Noddy_dealloc(Noddy * self)
{
	Py_XDECREF(self->first);
	Py_XDECREF(self->last);
	self->ob_type->tp_free((PyObject *)self);
}


/* constructor
 */

static PyObject *
Noddy_new(PyTypeObject *type, PyObject *args, PyObject *kwds)
{
	Noddy *self;
	self = (Noddy *)type->tp_alloc(type,0);
	if (self != NULL)
	{
		self->first = PyString_FromString("");
		if (self->first == NULL)
		{
			Py_DECREF(self);
			return NULL;
		}
		self->last = PyString_FromString("");
		if (self->first == NULL)
		{
			Py_DECREF(self);
			return NULL;
		}
		self->number = 0;		
	}
	return (PyObject *)self;
}

static int
Noddy_init(Noddy *self, PyObject *args, PyObject *kwds)
{
	PyObject *first=NULL, *last=NULL, *tmp;
	static char *kwlist[] = {"first","last","number",NULL};
	if (!PyArg_ParseTupleAndKeywords(args,kwds,"|OOi",kwlist,
									 &first,&last,
									 &self->number))
	{
		return -1;
	}
	if (first)
	{
		/* referencia el puntero
		 */
		tmp = self->first;
		Py_INCREF(first);
		self->first = first;
		/* y ahora lo libera...supongo que es porque 
		 * es posible que exista otro objeto antes en esa dirección
		 */
		Py_XDECREF(tmp);
	}
	if (last)
	{
		tmp = self->last;
		Py_INCREF(last);
		self->last = last;
		Py_XDECREF(tmp);
	}
	return 0;
}

/* se definen los miembros de la estructura
 */
 
static PyMemberDef Noddy_members[] = {
	{"first", T_OBJECT_EX, offsetof(Noddy, first), 0, "first name"},
	{"last", T_OBJECT_EX, offsetof(Noddy, last), 0, "last name"},
	{"number", T_INT, offsetof(Noddy, number), 0, "noddy number"},
	{NULL}	
};


static PyObject *
Noddy_name(Noddy *self)
{
	static PyObject *format = NULL;
	PyObject *args, *result;
	
	if (format == NULL)
	{
		format = PyString_FromString("%s %s");
		if (format == NULL)
		{
			return NULL;
		}
	}
	
	if (self->first == NULL)
	{
		PyErr_SetString(PyExc_AttributeError, "first");
		return NULL;
	}
	
	if (self->last == NULL)
	{
		PyErr_SetString(PyExc_AttributeError, "last");
		return NULL;
	}
	
	args = Py_BuildValue("OO", self->first, self->last);
	if (args == NULL)
	{
		return NULL;
	}
	
	result = PyString_Format(format, args);
	Py_DECREF(args);
	return result;
}

static PyMethodDef Noddy_methods[] = {
	{"name", (PyCFunction)Noddy_name, METH_NOARGS,
	 "Devuelve el nombre, compuesto por el primer y ultimo nombre"
	},
	{NULL}
};

PyTypeObject NoddyType = {
	PyObject_HEAD_INIT(NULL)
	0,					/* ob_size */
	"noddy.Noddy",				/* tp_name */
	sizeof(Noddy),			/* tp_basicsize */
	0,					/* tp_itemsize */
	/* methods */
	(destructor)Noddy_dealloc,		 		/* tp_dealloc */
	0,					/* tp_print */
	0,					/* tp_getattr */
	0,					/* tp_setattr */
	0,					/* tp_compare */
	0,				/* tp_repr */
	0,					/* tp_as_number */
	0,					/* tp_as_sequence */
	0,		       			/* tp_as_mapping */
	0,					/* tp_hash */
	0,					/* tp_call */
	0,					/* tp_str */
	0,				/* tp_getattro */
	0,					/* tp_setattro */
	0,					/* tp_as_buffer */
	Py_TPFLAGS_DEFAULT,		/* tp_flags */
 	"Noddy objects",				/* tp_doc */
 	0,				/* tp_traverse */
 	0,					/* tp_clear */
	0,					/* tp_richcompare */
	0,					/* tp_weaklistoffset */
	0,					/* tp_iter */
	0,					/* tp_iternext */
	Noddy_methods,					/* tp_methods */
	Noddy_members,				/* tp_members */
	0,					/* tp_getset */
	0,					/* tp_base */
	0,					/* tp_dict */
	0,			/* tp_descr_get */
	0,					/* tp_descr_set */
	0,					/* tp_dictoffset */
	(initproc)Noddy_init,				/* tp_init */
	0,			/* tp_alloc */
	Noddy_new,			/* tp_new */
	0,        		/* tp_free */
};

static PyMethodDef module_methods[] = {
    {NULL} /*sentinela*/
};

#ifndef PyMODINIT_FUNC
#define PyMODINIT_FUNC void
#endif
PyMODINIT_FUNC
initnoddy2(void)
{
    PyObject *module;
    if (PyType_Ready(&NoddyType) < 0)
        return;
    module = Py_InitModule3("noddy2",module_methods,
    "Ejemplo de un modulo que crea una extension de tipo");
    Py_INCREF(&NoddyType);
    PyModule_AddObject(module, "Noddy",(PyObject *)&NoddyType);
}


