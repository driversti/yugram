class ChatViewer {
    constructor() {
        this.chats = [];
        this.messages = [];
        this.currentChatId = null;
        this.currentChatPage = 0;
        this.chatPageSize = 20;
        this.messagesPage = 0;
        this.messagePageSize = 50;
        this.totalChatPages = 0;
        this.totalChats = 0;
        this.totalMessagePages = 0;
        this.totalMessages = 0;
        this.apiBaseUrl = 'http://localhost:8080';

        this.initializeElements();
        this.bindEvents();
        this.loadChats();
    }

    initializeElements() {
        this.chatsList = document.getElementById('chats-list');
        this.messagesContainer = document.getElementById('messages-container');
        this.chatTitle = document.getElementById('chat-title');
        this.refreshBtn = document.getElementById('refresh-chats');
        this.loadMoreBtn = document.getElementById('load-more');
        this.paginationInfo = document.getElementById('pagination-info');
        this.paginationControls = document.getElementById('pagination-controls');
    }

    bindEvents() {
        this.refreshBtn.addEventListener('click', () => this.refreshChats());
        this.loadMoreBtn.addEventListener('click', () => this.loadMoreMessages());
    }

  
    async loadChats(page = this.currentChatPage) {
        this.showLoading(this.chatsList);

        try {
            const response = await fetch(`${this.apiBaseUrl}/chats/withMessages?page=${page}&size=${this.chatPageSize}`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            this.chats = data.content || [];
            this.currentChatPage = data.page || 0;
            this.totalChatPages = data.totalPages || 0;
            this.totalChats = data.totalElements || 0;

            this.renderChats();
            this.renderPagination();
        } catch (error) {
            console.error('Error loading chats:', error);
            this.showError(this.chatsList, 'Failed to load chats. Make sure the application is running.');
        }
    }

    renderChats() {
        this.chatsList.innerHTML = '';

        this.chats.forEach(chat => {
            const chatElement = document.createElement('div');
            chatElement.className = 'chat-item';
            chatElement.dataset.chatId = chat.id;

            if (chat.id === this.currentChatId) {
                chatElement.classList.add('active');
            }

            const displayName = chat.title || `Chat ${chat.id}`;
            const chatType = chat.type ? `(${chat.type})` : '';

            chatElement.innerHTML = `
                <div class="chat-name">${displayName}</div>
                <div class="chat-id">ID: ${chat.id} ${chatType}</div>
            `;

            chatElement.addEventListener('click', () => this.selectChat(chat.id));
            this.chatsList.appendChild(chatElement);
        });
    }

    renderPagination() {
        // Update pagination info
        const startChat = this.totalChats > 0 ? this.currentChatPage * this.chatPageSize + 1 : 0;
        const endChat = Math.min((this.currentChatPage + 1) * this.chatPageSize, this.totalChats);
        this.paginationInfo.textContent = `Showing ${startChat}-${endChat} of ${this.totalChats} chats with messages`;

        // Clear existing pagination controls
        this.paginationControls.innerHTML = '';

        if (this.totalChatPages <= 1) {
            return; // No pagination needed
        }

        // Previous button
        const prevBtn = this.createPaginationButton('‹', this.currentChatPage - 1, this.currentChatPage === 0);
        prevBtn.classList.add('nav-btn');
        this.paginationControls.appendChild(prevBtn);

        // Page numbers
        const maxVisiblePages = 7;
        let startPage = Math.max(0, this.currentChatPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(this.totalChatPages, startPage + maxVisiblePages);

        if (endPage - startPage < maxVisiblePages) {
            startPage = Math.max(0, endPage - maxVisiblePages);
        }

        // First page and ellipsis
        if (startPage > 0) {
            this.paginationControls.appendChild(this.createPaginationButton('1', 0, false));
            if (startPage > 1) {
                const ellipsis = document.createElement('span');
                ellipsis.className = 'pagination-ellipsis';
                ellipsis.textContent = '...';
                this.paginationControls.appendChild(ellipsis);
            }
        }

        // Page range
        for (let i = startPage; i < endPage; i++) {
            const btn = this.createPaginationButton((i + 1).toString(), i, false);
            if (i === this.currentChatPage) {
                btn.classList.add('active');
            }
            this.paginationControls.appendChild(btn);
        }

        // Last page and ellipsis
        if (endPage < this.totalChatPages) {
            if (endPage < this.totalChatPages - 1) {
                const ellipsis = document.createElement('span');
                ellipsis.className = 'pagination-ellipsis';
                ellipsis.textContent = '...';
                this.paginationControls.appendChild(ellipsis);
            }
            this.paginationControls.appendChild(this.createPaginationButton(this.totalChatPages.toString(), this.totalChatPages - 1, false));
        }

        // Next button
        const nextBtn = this.createPaginationButton('›', this.currentChatPage + 1, this.currentChatPage === this.totalChatPages - 1);
        nextBtn.classList.add('nav-btn');
        this.paginationControls.appendChild(nextBtn);
    }

    createPaginationButton(text, page, disabled) {
        const button = document.createElement('button');
        button.className = 'pagination-btn';
        button.textContent = text;
        button.disabled = disabled;

        if (!disabled) {
            button.addEventListener('click', () => this.goToPage(page));
        }

        return button;
    }

    goToPage(page) {
        if (page >= 0 && page < this.totalChatPages && page !== this.currentChatPage) {
            this.loadChats(page);
        }
    }

    refreshChats() {
        this.currentChatPage = 0;
        this.loadChats(0);
    }

    async selectChat(chatId) {
        // Update active state
        document.querySelectorAll('.chat-item').forEach(item => {
            item.classList.remove('active');
        });
        document.querySelector(`[data-chat-id="${chatId}"]`).classList.add('active');

        this.currentChatId = chatId;
        this.messagesPage = 0;
        this.messages = [];

        const chat = this.chats.find(c => c.id === chatId);
        const displayName = chat ? (chat.title || `Chat ${chat.id}`) : `Chat ${chatId}`;
        this.chatTitle.textContent = displayName;

        await this.loadMessages();
    }

    async loadMessages(page = this.messagesPage) {
        if (!this.currentChatId) {
            return;
        }

        this.showLoading(this.messagesContainer);

        try {
            const response = await fetch(`${this.apiBaseUrl}/messages/byChatId/${this.currentChatId}?page=${page}&size=${this.messagePageSize}`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            const newMessages = data.content || [];

            if (page === 0) {
                this.messages = newMessages;
                this.renderMessages(true);
            } else {
                // Prepend older messages for pagination
                this.messages = [...newMessages, ...this.messages];
                this.renderMessages(false);
            }

            this.messagesPage = data.page || 0;
            this.totalMessagePages = data.totalPages || 0;
            this.totalMessages = data.totalElements || 0;

            this.updateLoadMoreButton();
        } catch (error) {
            console.error('Error loading messages:', error);
            this.showError(this.messagesContainer, 'Failed to load messages. Please try again.');
            this.loadMoreBtn.style.display = 'none';
        }
    }

    loadMoreMessages() {
        if (this.messagesPage < this.totalMessagePages - 1) {
            this.loadMessages(this.messagesPage + 1);
        }
    }

    updateLoadMoreButton() {
        if (this.messagesPage < this.totalMessagePages - 1) {
            this.loadMoreBtn.style.display = 'block';
            this.loadMoreBtn.textContent = `Load More (${this.totalMessages - this.messages.length} remaining)`;
        } else {
            this.loadMoreBtn.style.display = 'none';
        }
    }

    renderMessages(clearContainer = true) {
        if (clearContainer) {
            this.messagesContainer.innerHTML = '';

            if (this.messages.length === 0) {
                this.messagesContainer.innerHTML = `
                    <div class="empty-state">
                        <p>No messages found for this chat</p>
                    </div>
                `;
                return;
            }

            // Sort messages by date (newest last for chat-like display)
            const sortedMessages = [...this.messages].sort((a, b) => a.date - b.date);

            sortedMessages.forEach(message => {
                const messageElement = this.createMessageElement(message);
                this.messagesContainer.appendChild(messageElement);
            });

            // Scroll to bottom (newest messages)
            this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
        } else {
            // Loading more messages (older ones)
            const fragment = document.createDocumentFragment();
            const newMessages = this.messages.slice(0, this.messagePageSize);

            // Sort new messages by date (oldest first for prepending)
            const sortedNewMessages = [...newMessages].sort((a, b) => a.date - b.date);

            sortedNewMessages.forEach(message => {
                const messageElement = this.createMessageElement(message);
                fragment.appendChild(messageElement);
            });

            this.messagesContainer.insertBefore(fragment, this.messagesContainer.firstChild);

            // Maintain scroll position by adjusting to the height of added content
            const scrollHeight = this.messagesContainer.scrollHeight;
            const scrollTop = this.messagesContainer.scrollTop;
            this.messagesContainer.scrollTop = scrollTop + (this.messagesContainer.scrollHeight - scrollHeight);
        }
    }

    createMessageElement(message) {
        const messageDiv = document.createElement('div');

        // Determine if message is from current user (assuming senderId equals chatId means it's our message)
        // This is a simplified logic - you might need to adjust based on your actual user ID detection
        const isFromUser = message.senderId === this.currentChatId;
        messageDiv.className = `message ${isFromUser ? 'sent' : 'received'}`;

        // Convert Unix timestamp to readable time
        const date = new Date(message.date * 1000); // Convert to milliseconds
        const time = date.toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit'
        });

        const dateStr = date.toLocaleDateString([], {
            month: 'short',
            day: 'numeric'
        });

        messageDiv.innerHTML = `
            <div class="message-text">${this.escapeHtml(message.content || 'No content')}</div>
            <div class="message-time">${dateStr} ${time}</div>
        `;

        return messageDiv;
    }

    showLoading(container) {
        container.innerHTML = '<div class="loading">Loading...</div>';
    }

    showError(container, message) {
        container.innerHTML = `<div class="error">Error: ${message}</div>`;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
}

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    new ChatViewer();
});