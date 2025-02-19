package xcj.app.main.dao.mysql

//@RepositoryDefinition(domainClass = Friend::class, idClass = Int::class)
//等价于继承CrudRepository<User, Int>
/*
class FriendRepository(
    private val friendDao: FriendDao,
    private val userDao: UserDao
    ): PagingAndSortingRepository<Friend, Int> {
    override fun <S : Friend> save(entity: S): S {
        val addFriendResult = friendDao.addFriend(entity.uid, entity.friendUid)
        if(addFriendResult==1)
            return entity
        else throw Exception()
    }

    override fun <S : Friend> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        var allSize = 0
        var addSumCount = 0
        val userIdToFriends = entities.groupBy {
            allSize++
            it.uid
        }
        userIdToFriends.forEach { (uid, friends) ->
            addSumCount += if(friends.size==1){
                val addFriendResult = friendDao.addFriend(uid, friends[0].friendUid)
                addFriendResult
            } else{
                val addFriendsResult = friendDao.addFriends(uid, friends.map { it.friendUid })
                addFriendsResult
            }
        }
        if(addSumCount==allSize)
            return entities
        else throw Exception()
    }

    override fun findById(id: Int): Optional<Friend> {
        return Optional.ofNullable(friendDao.getFriend(id))

    }

    override fun existsById(id: String): Boolean {
        return friendDao.getFriend(id) != null
    }

    override fun findAll(sort: Sort): MutableIterable<Friend> {
        return friendDao.getAllFriends().toMutableList()
    }

    override fun findAll(pageable: Pageable): Page<Friend> {
        val friendsPaged = friendDao.getFriendsPaged(pageable.pageNumber, pageable.pageSize)
        return PageImpl(friendsPaged)
    }

    override fun findAll(): MutableIterable<Friend> {
        return friendDao.getAllFriends().toMutableList()
    }

    override fun findAllById(ids: MutableIterable<String>): MutableIterable<Friend> {
        friendDao.getFriendsByFriendId().toMutableList()
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    override fun delete(entity: Friend) {
        TODO("Not yet implemented")
    }

    override fun deleteAllById(ids: MutableIterable<String>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll(entities: MutableIterable<Friend>) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }
}*/
