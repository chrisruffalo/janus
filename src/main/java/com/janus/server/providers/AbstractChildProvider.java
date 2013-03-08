package com.janus.server.providers;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import com.janus.model.Author;
import com.janus.model.BaseEntity;
import com.janus.model.Book;
import com.janus.model.Series;
import com.janus.model.Tag;
import com.janus.model.interfaces.ISorted;

public abstract class AbstractChildProvider<E extends BaseEntity> extends AbstractBaseEntityProvider<E> {
	
	@Inject
	private FileInfoProvider fileInfoProvider;
	
	@Inject
	private Logger logger;
	
	@Inject
	private EntityManager manager;
	
	protected abstract String getJoinField();
	
	public List<Book> getBooksForChild(Long childId, int index, int size) {
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<Book> query = builder.createQuery(Book.class);
		
		Root<Book> bookRoot = query.from(Book.class);
		query.select(bookRoot);
		
		// join onto target input type
		Join<Book, E> joinToInput = bookRoot.join(this.getJoinField());
		
		// identifier on the join type has to match what was passed in
		query.where(builder.equal(joinToInput.get(BaseEntity.ID), childId));

		// get the name of the series, "aaaa" if null to put it at the 
		// top of the list.  this mimicks earlier behavior
		Expression<String> seriesName = builder.coalesce(
			bookRoot.join(Book.SERIES, JoinType.LEFT).get(Series.SORT).as(String.class), 
			"aaaa"
		);

		// order by series, series id, and sort name
		Order orderBySeriesName = builder.asc(seriesName);
		Order orderBySeriesId = builder.asc(bookRoot.get(Book.MODEL_SERIESINDEX));
		Order orderBySortName = builder.asc(bookRoot.get(Book.SORT));
				
		query.orderBy(
			orderBySeriesName,
			orderBySeriesId, 
			orderBySortName
		);
		
		// now that the query is ready...
		return this.executeRangeQuery(query, index, size);
	}
	
	public List<Series> getSeriesForChild(Long authorId, int index, int size) {
		
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<Series> query = builder.createQuery(Series.class);
		
		// roots
		Root<Book> bookRoot = query.from(Book.class);
		Join<Book, Series> seriesJoin = bookRoot.join(Book.SERIES);
		Join<Book, E> authorJoin = bookRoot.join(this.getJoinField());
		
		// author id equals incoming id
		query.where(builder.equal(authorJoin.get(BaseEntity.ID), authorId));

		// order
		query.orderBy(builder.asc(seriesJoin.get(ISorted.SORT)));
		
		// select only series
		query.distinct(true);
		query.select(seriesJoin);
		
		// execute query
		return this.executeRangeQuery(query, index, size);
	}
	
	public List<Tag> getTagsForChild(Long authorId, int index, int size) {
		
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<Tag> query = builder.createQuery(Tag.class);
		
		// roots
		Root<Book> bookRoot = query.from(Book.class);
		Join<Book, Tag> tagsJoin = bookRoot.join(Book.TAGS);
		Join<Book, E> authorJoin = bookRoot.join(this.getJoinField());
		
		// author id equals incoming id
		query.where(builder.equal(authorJoin.get(BaseEntity.ID), authorId));

		// order
		query.orderBy(builder.asc(tagsJoin.get(ISorted.SORT)));
		
		// select only series
		query.distinct(true);
		query.select(tagsJoin);
		
		// execute query
		return this.executeRangeQuery(query, index, size);
	}
	
	public List<Author> getAuthorsForChild(Long authorId, int index, int size) {
		
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<Author> query = builder.createQuery(Author.class);
		
		// roots
		Root<Book> bookRoot = query.from(Book.class);
		Join<Book, Author> authorsJoin = bookRoot.join(Book.AUTHORS);
		Join<Book, E> authorJoin = bookRoot.join(this.getJoinField());
		
		// author id equals incoming id
		query.where(builder.equal(authorJoin.get(BaseEntity.ID), authorId));

		// order
		query.orderBy(builder.asc(authorsJoin.get(ISorted.SORT)));
		
		// select only series
		query.distinct(true);
		query.select(authorsJoin);
		
		// execute query
		return this.executeRangeQuery(query, index, size);
	}
	
	public Long getRandomParentBookId(Long childId) {
		CriteriaBuilder builder = this.manager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		
		Root<Book> bookRoot = query.from(Book.class);
		query.select(bookRoot.get(Book.ID).as(Long.class));
		query.distinct(true);
		
		// join onto target input type
		Join<Book, E> joinToInput = bookRoot.join(this.getJoinField(), JoinType.LEFT);
		
		// identifier on the join type has to match what was passed in
		query.where(builder.equal(joinToInput.get(BaseEntity.ID), childId));
		
		// exceute id-only query
		List<Long> bookIdList = this.executeQuery(query);
		
		// return null value
		if(bookIdList == null || bookIdList.isEmpty()) {
			return null;
		}
		
		// if only one, return one
		if(bookIdList.size() == 1) {
			return bookIdList.get(0);
		}
		
		// otherwise, choose random
		Random random = new Random(System.nanoTime());
		int randomElementNumber = random.nextInt(bookIdList.size() - 1);
		
		// return random (or near enough) element
		return bookIdList.get(randomElementNumber);
	}
	
	public byte[] getRandomCover(Long childId, boolean encodeInBase64, int width, int height) {
		Long randomId = this.getRandomParentBookId(childId);
		return this.fileInfoProvider.getCoverDataForBook(randomId, encodeInBase64, width, height);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean setUpSort(Root<E> root, CriteriaBuilder builder, CriteriaQuery<E> query, String sortString) {
		boolean result = super.setUpSort(root, builder, query, sortString);
		
		// if already sorted, leave
		if(result) {
			return true;
		}

		// implement specific book count strategies here 
		
		return false;
	}
}
