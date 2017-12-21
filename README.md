When you speak of FileBlobStore we associate it to the Datastore which is "different" from the NodeStore.
The NodeStore is the part you think about clustering using a DocumentNodeStore implementation. 
You can configure any of our DocumentNS implementation (Mongo and RDB) with a FileBlobStore. 
When it comes to clustering you can then decide to dedicate a FileBlobStore for each individual cluster node, 
having therefore multiple copies of the same binary, or opt for a shared Datastore to be used by all the cluster nodes.


<b>How do i specify the dedicated FileBlobStore vs the shared Datastore? I read the oak guide and i tried to find an example on internet but i did not find anything.</b>

Have a shared filesystem between all the boxes, mount it, and reference it as path in the FileBlobStore configuration. 
It may differ a bit but the concept was that. It goes by its own that the type of shared drive will make a big difference.
For example, while an NFS for the Datastore may work, it won't be that performant. 
Please also note that we don't recommend use NFS as of latency. And if you really have to use it, use it ONLY for the Datastore. NEVER for the Nodestore (in case of Segment).


<b>Any recommendations / things to avoid using the RDBDocumentStore? </b>

No such list as of now. However do ensure that binaries are not stored in Database and you make use of any external DataStore 